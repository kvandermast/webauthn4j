/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package integration.scenario;

import com.webauthn4j.WebAuthnManager;
import com.webauthn4j.anchor.TrustAnchorsResolver;
import com.webauthn4j.converter.AuthenticationExtensionsClientOutputsConverter;
import com.webauthn4j.converter.util.ObjectConverter;
import com.webauthn4j.data.*;
import com.webauthn4j.data.attestation.statement.COSEAlgorithmIdentifier;
import com.webauthn4j.data.client.Origin;
import com.webauthn4j.data.client.challenge.Challenge;
import com.webauthn4j.data.client.challenge.DefaultChallenge;
import com.webauthn4j.data.extension.client.*;
import com.webauthn4j.server.ServerProperty;
import com.webauthn4j.test.EmulatorUtil;
import com.webauthn4j.test.TestAttestationUtil;
import com.webauthn4j.test.authenticator.webauthn.WebAuthnAuthenticatorAdaptor;
import com.webauthn4j.test.client.ClientPlatform;
import com.webauthn4j.validator.attestation.statement.androidkey.AndroidKeyAttestationStatementValidator;
import com.webauthn4j.validator.attestation.statement.none.NoneAttestationStatementValidator;
import com.webauthn4j.validator.attestation.statement.packed.PackedAttestationStatementValidator;
import com.webauthn4j.validator.attestation.statement.u2f.FIDOU2FAttestationStatementValidator;
import com.webauthn4j.validator.attestation.trustworthiness.certpath.TrustAnchorCertPathTrustworthinessValidator;
import com.webauthn4j.validator.attestation.trustworthiness.ecdaa.DefaultECDAATrustworthinessValidator;
import com.webauthn4j.validator.attestation.trustworthiness.self.DefaultSelfAttestationTrustworthinessValidator;
import com.webauthn4j.validator.exception.UnexpectedExtensionException;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserVerifyingAuthenticatorRegistrationValidationTest {

    private ObjectConverter objectConverter = new ObjectConverter();

    private Origin origin = new Origin("http://localhost");
    private WebAuthnAuthenticatorAdaptor webAuthnAuthenticatorAdaptor = new WebAuthnAuthenticatorAdaptor(EmulatorUtil.PACKED_AUTHENTICATOR);
    private ClientPlatform clientPlatform = new ClientPlatform(origin, webAuthnAuthenticatorAdaptor);
    private NoneAttestationStatementValidator noneAttestationStatementValidator = new NoneAttestationStatementValidator();
    private PackedAttestationStatementValidator packedAttestationStatementValidator = new PackedAttestationStatementValidator();
    private FIDOU2FAttestationStatementValidator fidoU2FAttestationStatementValidator = new FIDOU2FAttestationStatementValidator();
    private AndroidKeyAttestationStatementValidator androidKeyAttestationStatementValidator = new AndroidKeyAttestationStatementValidator();
    private TrustAnchorsResolver trustAnchorsResolver = TestAttestationUtil.createTrustAnchorProviderWith3tierTestRootCACertificate();
    private WebAuthnManager target = new WebAuthnManager(
            Arrays.asList(
                    noneAttestationStatementValidator,
                    packedAttestationStatementValidator,
                    fidoU2FAttestationStatementValidator,
                    androidKeyAttestationStatementValidator),
            new TrustAnchorCertPathTrustworthinessValidator(trustAnchorsResolver),
            new DefaultECDAATrustworthinessValidator(),
            new DefaultSelfAttestationTrustworthinessValidator(),
            objectConverter
    );

    private AuthenticationExtensionsClientOutputsConverter authenticationExtensionsClientOutputsConverter
            = new AuthenticationExtensionsClientOutputsConverter(objectConverter);

    @Test
    void validate_WebAuthnRegistrationContext_with_none_attestation_statement_test() {
        String rpId = "example.com";
        Challenge challenge = new DefaultChallenge();
        AuthenticatorSelectionCriteria authenticatorSelectionCriteria =
                new AuthenticatorSelectionCriteria(
                        AuthenticatorAttachment.CROSS_PLATFORM,
                        true,
                        UserVerificationRequirement.REQUIRED);

        PublicKeyCredentialParameters publicKeyCredentialParameters = new PublicKeyCredentialParameters(PublicKeyCredentialType.PUBLIC_KEY, COSEAlgorithmIdentifier.ES256);

        PublicKeyCredentialUserEntity publicKeyCredentialUserEntity = new PublicKeyCredentialUserEntity();

        AuthenticationExtensionsClientInputs<RegistrationExtensionClientInput> extensions = new AuthenticationExtensionsClientInputs<>();
        PublicKeyCredentialCreationOptions credentialCreationOptions
                = new PublicKeyCredentialCreationOptions(
                new PublicKeyCredentialRpEntity(rpId, "example.com"),
                publicKeyCredentialUserEntity,
                challenge,
                Collections.singletonList(publicKeyCredentialParameters),
                null,
                Collections.emptyList(),
                authenticatorSelectionCriteria,
                AttestationConveyancePreference.NONE,
                extensions
        );
        PublicKeyCredential<AuthenticatorAttestationResponse, RegistrationExtensionClientOutput> credential = clientPlatform.create(credentialCreationOptions);
        AuthenticatorAttestationResponse registrationRequest = credential.getAuthenticatorResponse();
        AuthenticationExtensionsClientOutputs clientExtensionResults = credential.getClientExtensionResults();
        Set<String> transports = Collections.emptySet();
        String clientExtensionJSON = authenticationExtensionsClientOutputsConverter.convertToString(clientExtensionResults);
        ServerProperty serverProperty = new ServerProperty(origin, rpId, challenge, null);
        WebAuthnRegistrationRequest webAuthnRegistrationRequest
                = new WebAuthnRegistrationRequest(
                registrationRequest.getAttestationObject(),
                registrationRequest.getClientDataJSON(),
                clientExtensionJSON,
                transports
        );
        WebAuthnRegistrationParameters webAuthnRegistrationParameters = new WebAuthnRegistrationParameters(
                serverProperty,
                false
        );


        WebAuthnRegistrationData webAuthnRegistrationData = target.parseRegistrationRequest(webAuthnRegistrationRequest);
        target.validate(webAuthnRegistrationData, webAuthnRegistrationParameters);

        assertAll(
                () -> assertThat(webAuthnRegistrationData.getCollectedClientData()).isNotNull(),
                () -> assertThat(webAuthnRegistrationData.getAttestationObject()).isNotNull(),
                () -> assertThat(webAuthnRegistrationData.getClientExtensions()).isNotNull()
        );
    }

    @Test
    void validate_WebAuthnRegistrationContext_with_packed_attestation_statement_test() {
        String rpId = "example.com";
        Challenge challenge = new DefaultChallenge();
        AuthenticatorSelectionCriteria authenticatorSelectionCriteria =
                new AuthenticatorSelectionCriteria(
                        AuthenticatorAttachment.CROSS_PLATFORM,
                        true,
                        UserVerificationRequirement.REQUIRED);

        PublicKeyCredentialParameters publicKeyCredentialParameters = new PublicKeyCredentialParameters(PublicKeyCredentialType.PUBLIC_KEY, COSEAlgorithmIdentifier.ES256);

        PublicKeyCredentialUserEntity publicKeyCredentialUserEntity = new PublicKeyCredentialUserEntity();

        AuthenticationExtensionsClientInputs<RegistrationExtensionClientInput> extensions = new AuthenticationExtensionsClientInputs<>();
        PublicKeyCredentialCreationOptions credentialCreationOptions
                = new PublicKeyCredentialCreationOptions(
                new PublicKeyCredentialRpEntity(rpId, "example.com"),
                publicKeyCredentialUserEntity,
                challenge,
                Collections.singletonList(publicKeyCredentialParameters),
                null,
                Collections.emptyList(),
                authenticatorSelectionCriteria,
                AttestationConveyancePreference.DIRECT,
                extensions
        );

        PublicKeyCredential<AuthenticatorAttestationResponse, RegistrationExtensionClientOutput> credential = clientPlatform.create(credentialCreationOptions);
        AuthenticatorAttestationResponse registrationRequest = credential.getAuthenticatorResponse();
        AuthenticationExtensionsClientOutputs clientExtensionResults = credential.getClientExtensionResults();
        Set<String> transports = Collections.emptySet();
        String clientExtensionJSON = authenticationExtensionsClientOutputsConverter.convertToString(clientExtensionResults);
        ServerProperty serverProperty = new ServerProperty(origin, rpId, challenge, null);
        WebAuthnRegistrationRequest webAuthnRegistrationRequest
                = new WebAuthnRegistrationRequest(
                registrationRequest.getAttestationObject(),
                registrationRequest.getClientDataJSON(),
                clientExtensionJSON,
                transports
        );
        WebAuthnRegistrationParameters webAuthnRegistrationParameters = new WebAuthnRegistrationParameters(
                serverProperty,
                false
        );


        WebAuthnRegistrationData webAuthnRegistrationData = target.validate(webAuthnRegistrationRequest, webAuthnRegistrationParameters);
        target.validate(webAuthnRegistrationData, webAuthnRegistrationParameters);

        assertAll(
                () -> assertThat(webAuthnRegistrationData.getCollectedClientData()).isNotNull(),
                () -> assertThat(webAuthnRegistrationData.getAttestationObject()).isNotNull(),
                () -> assertThat(webAuthnRegistrationData.getClientExtensions()).isNotNull()
        );
    }

    @Test
    void validate_WebAuthnRegistrationContext_with_unexpected_extension_test() {
        String rpId = "example.com";
        Challenge challenge = new DefaultChallenge();
        AuthenticatorSelectionCriteria authenticatorSelectionCriteria =
                new AuthenticatorSelectionCriteria(
                        AuthenticatorAttachment.CROSS_PLATFORM,
                        true,
                        UserVerificationRequirement.REQUIRED);

        PublicKeyCredentialParameters publicKeyCredentialParameters = new PublicKeyCredentialParameters(PublicKeyCredentialType.PUBLIC_KEY, COSEAlgorithmIdentifier.ES256);

        PublicKeyCredentialUserEntity publicKeyCredentialUserEntity = new PublicKeyCredentialUserEntity();

        Map<String, RegistrationExtensionClientInput> extensions = new HashMap<>();
        extensions.put(SupportedExtensionsExtensionClientInput.ID, new SupportedExtensionsExtensionClientInput(true));

        PublicKeyCredentialCreationOptions credentialCreationOptions
                = new PublicKeyCredentialCreationOptions(
                new PublicKeyCredentialRpEntity(rpId, "example.com"),
                publicKeyCredentialUserEntity,
                challenge,
                Collections.singletonList(publicKeyCredentialParameters),
                null,
                Collections.emptyList(),
                authenticatorSelectionCriteria,
                AttestationConveyancePreference.NONE,
                new AuthenticationExtensionsClientInputs<>(extensions)
        );

        PublicKeyCredential<AuthenticatorAttestationResponse, RegistrationExtensionClientOutput> credential = clientPlatform.create(credentialCreationOptions);
        AuthenticatorAttestationResponse registrationRequest = credential.getAuthenticatorResponse();
        AuthenticationExtensionsClientOutputs clientExtensionResults = credential.getClientExtensionResults();
        Set<String> transports = Collections.emptySet();
        String clientExtensionJSON = authenticationExtensionsClientOutputsConverter.convertToString(clientExtensionResults);

        ServerProperty serverProperty = new ServerProperty(origin, rpId, challenge, null);
        List<String> expectedExtensions = Collections.singletonList("uvm");
        WebAuthnRegistrationRequest webAuthnRegistrationRequest
                = new WebAuthnRegistrationRequest(
                registrationRequest.getAttestationObject(),
                registrationRequest.getClientDataJSON(),
                clientExtensionJSON,
                transports
        );
        WebAuthnRegistrationParameters webAuthnRegistrationParameters = new WebAuthnRegistrationParameters(
                serverProperty,
                false,
                true,
                expectedExtensions
        );

        assertThrows(UnexpectedExtensionException.class,
                () -> {
                    WebAuthnRegistrationData webAuthnRegistrationData = target.parseRegistrationRequest(webAuthnRegistrationRequest);
                    target.validate(webAuthnRegistrationData, webAuthnRegistrationParameters);
                }
        );
    }
}

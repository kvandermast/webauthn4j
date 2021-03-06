/*
 * Copyright 2002-2018 the original author or authors.
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

package com.webauthn4j.test.authenticator.u2f;

import com.webauthn4j.util.WIP;

@WIP
public class AuthenticationRequest {

    private byte control;
    private byte[] challenge;
    private byte[] applicationParameter;
    private byte[] keyHandle;

    public AuthenticationRequest(byte control, byte[] challenge, byte[] applicationParameter, byte[] keyHandle) {
        if (challenge.length != 32) {
            throw new IllegalArgumentException("challenge must be 32 bytes");
        }
        if (applicationParameter.length != 32) {
            throw new IllegalArgumentException("applicationParameter must be 32 bytes");
        }

        this.control = control;
        this.challenge = challenge;
        this.applicationParameter = applicationParameter;
        this.keyHandle = keyHandle;
    }

    public byte getControl() {
        return control;
    }

    public byte[] getChallenge() {
        return challenge;
    }

    public byte[] getApplicationParameter() {
        return applicationParameter;
    }

    public byte[] getKeyHandle() {
        return keyHandle;
    }

}

package com.webauthn4j.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.webauthn4j.client.CollectedClientData;
import com.webauthn4j.util.jackson.WebAuthnModule;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

public class CollectedClientDataConverter {

    private ObjectMapper objectMapper;

    public CollectedClientDataConverter() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new WebAuthnModule());
    }

    public CollectedClientData convert(String base64UrlString) {
        byte[] bytes = java.util.Base64.getUrlDecoder().decode(base64UrlString);
        return convert(bytes);
    }

    public CollectedClientData convert(byte[] source){
        String jsonString = new String(source, StandardCharsets.UTF_8);
        try {
            return objectMapper.readValue(jsonString, CollectedClientData.class);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
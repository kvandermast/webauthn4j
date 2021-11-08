package com.webauthn4j.data;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.webauthn4j.converter.exception.DataConversionException;
import com.webauthn4j.converter.jackson.deserializer.json.TransactionConfirmationDisplayFromStringDeserializer;
import com.webauthn4j.converter.util.JsonConverter;
import com.webauthn4j.converter.util.ObjectConverter;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

class TransactionConfirmationDisplayTest {

    private final ObjectConverter objectConverter = new ObjectConverter();
    private final JsonConverter jsonConverter = objectConverter.getJsonConverter();

    @Test
    void create_test() {
        assertAll(
                () -> assertThat(TransactionConfirmationDisplay.create(0x0001)).isEqualTo(TransactionConfirmationDisplay.ANY),
                () -> assertThat(TransactionConfirmationDisplay.create(0x0002)).isEqualTo(TransactionConfirmationDisplay.PRIVILEGED_SOFTWARE),
                () -> assertThat(TransactionConfirmationDisplay.create(0x0004)).isEqualTo(TransactionConfirmationDisplay.TEE),
                () -> assertThat(TransactionConfirmationDisplay.create(0x0008)).isEqualTo(TransactionConfirmationDisplay.HARDWARE),
                () -> assertThat(TransactionConfirmationDisplay.create(0x0010)).isEqualTo(TransactionConfirmationDisplay.REMOTE),
                () -> assertThat(TransactionConfirmationDisplay.create("any")).isEqualTo(TransactionConfirmationDisplay.ANY),
                () -> assertThat(TransactionConfirmationDisplay.create("privileged_software")).isEqualTo(TransactionConfirmationDisplay.PRIVILEGED_SOFTWARE),
                () -> assertThat(TransactionConfirmationDisplay.create("tee")).isEqualTo(TransactionConfirmationDisplay.TEE),
                () -> assertThat(TransactionConfirmationDisplay.create("hardware")).isEqualTo(TransactionConfirmationDisplay.HARDWARE),
                () -> assertThat(TransactionConfirmationDisplay.create("remote")).isEqualTo(TransactionConfirmationDisplay.REMOTE)
        );
    }

    @Test
    void getValue_test() {
        assertThat(TransactionConfirmationDisplay.ANY.getValue()).isEqualTo(0x0001);
    }

    @Test
    void toString_test() {
        assertThat(TransactionConfirmationDisplay.ANY.toString()).isEqualTo("any");
    }

    @Nested
    class IntSerialization {

        @Test
        void deserialize_test() {
            TransactionConfirmationDisplayTest.IntSerializationTestDTO dto = jsonConverter.readValue("{\"transactionConfirmationDisplay\": 1}", TransactionConfirmationDisplayTest.IntSerializationTestDTO.class);
            assertThat(dto.transactionConfirmationDisplay).isEqualTo(TransactionConfirmationDisplay.ANY);
        }

        @Test
        void deserialize_test_with_out_of_range_value() {
            assertThatThrownBy(
                    () -> jsonConverter.readValue("{\"transactionConfirmationDisplay\": \"-1\"}", TransactionConfirmationDisplayTest.IntSerializationTestDTO.class)
            ).isInstanceOf(DataConversionException.class);
        }

        @Test
        void deserialize_test_with_invalid_value() {
            assertThatThrownBy(
                    () -> jsonConverter.readValue("{\"transactionConfirmationDisplay\": \"\"}", TransactionConfirmationDisplayTest.IntSerializationTestDTO.class)
            ).isInstanceOf(DataConversionException.class);
        }

        @Test
        void deserialize_test_with_null() {
            TransactionConfirmationDisplayTest.IntSerializationTestDTO data = jsonConverter.readValue("{\"transactionConfirmationDisplay\":null}", TransactionConfirmationDisplayTest.IntSerializationTestDTO.class);
            assertThat(data.transactionConfirmationDisplay).isNull();
        }

    }

    static class IntSerializationTestDTO {
        @SuppressWarnings("WeakerAccess")
        public TransactionConfirmationDisplay transactionConfirmationDisplay;
    }

    @Nested
    class StringSerialization {

        @Test
        void deserialize_test() {
            TransactionConfirmationDisplayTest.StringSerializationTestDTO dto = jsonConverter.readValue("{\"transactionConfirmationDisplay\": \"any\"}", TransactionConfirmationDisplayTest.StringSerializationTestDTO.class);
            assertThat(dto.transactionConfirmationDisplay).isEqualTo(TransactionConfirmationDisplay.ANY);
        }

        @Test
        void deserialize_test_with_invalid_value() {
            assertThatThrownBy(
                    () -> jsonConverter.readValue("{\"transactionConfirmationDisplay\": \"invalid\"}", TransactionConfirmationDisplayTest.StringSerializationTestDTO.class)
            ).isInstanceOf(DataConversionException.class);
        }

        @Test
        void deserialize_test_with_null() {
            TransactionConfirmationDisplayTest.StringSerializationTestDTO data = jsonConverter.readValue("{\"transactionConfirmationDisplay\":null}", TransactionConfirmationDisplayTest.StringSerializationTestDTO.class);
            assertThat(data.transactionConfirmationDisplay).isNull();
        }

    }

    static class StringSerializationTestDTO {
        @JsonDeserialize(using = TransactionConfirmationDisplayFromStringDeserializer.class)
        @SuppressWarnings("WeakerAccess")
        public TransactionConfirmationDisplay transactionConfirmationDisplay;
    }


}
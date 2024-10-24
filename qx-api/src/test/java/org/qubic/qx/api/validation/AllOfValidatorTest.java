package org.qubic.qx.api.validation;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class AllOfValidatorTest {

    @SuppressWarnings("DataFlowIssue")
    @Test
    void isValid() {
        AllOfValidator validator = new AllOfValidator() {
            @Override
            public void initialize(AllOf constraintAnnotation) {
                allowedValues = new int[]{42, 666};
            }
        };
        validator.initialize(null);
        assertThat(validator.isValid(List.of(42), mock())).isTrue();
        assertThat(validator.isValid(List.of(42, 666), mock())).isTrue();
        assertThat(validator.isValid(List.of(43), mock())).isFalse();
        assertThat(validator.isValid(List.of(43, 44), mock())).isFalse();
        assertThat(validator.isValid(List.of(42, 43), mock())).isFalse();
    }
}
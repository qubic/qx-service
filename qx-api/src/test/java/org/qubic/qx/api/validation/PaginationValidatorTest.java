package org.qubic.qx.api.validation;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;

import static org.assertj.core.api.Assertions.assertThat;

class PaginationValidatorTest {

    private final PaginationValidator validator = new PaginationValidator();

    @Test
    void isValidTotalSize() {
        assertThat(validator.isValid(PageRequest.of(0, 1000), null)).isTrue();
        assertThat(validator.isValid(PageRequest.of(0, 100), null)).isTrue();
        assertThat(validator.isValid(PageRequest.of(1, 500), null)).isTrue();
        assertThat(validator.isValid(PageRequest.of(2, 330), null)).isTrue();
        assertThat(validator.isValid(PageRequest.of(3, 250), null)).isTrue();
        assertThat(validator.isValid(PageRequest.of(4, 200), null)).isTrue();
        assertThat(validator.isValid(PageRequest.of(5, 160), null)).isTrue();
        assertThat(validator.isValid(PageRequest.of(9, 100), null)).isTrue();
    }

    @Test
    void isValid_givenNoPaging_thenFalse() {
        assertThat(validator.isValid(null, null)).isFalse();
    }

    @Test
    void isValid_givenInvalidTotalSize_thenFalse() {
        assertThat(validator.isValid(PageRequest.of(0, 1001), null)).isFalse();
        assertThat(validator.isValid(PageRequest.of(1, 510), null)).isFalse();
        assertThat(validator.isValid(PageRequest.of(2, 340), null)).isFalse();
        assertThat(validator.isValid(PageRequest.of(3, 260), null)).isFalse();
        assertThat(validator.isValid(PageRequest.of(4, 210), null)).isFalse();
        assertThat(validator.isValid(PageRequest.of(5, 170), null)).isFalse();
        assertThat(validator.isValid(PageRequest.of(10, 100), null)).isFalse();
        assertThat(validator.isValid(PageRequest.of(100, 10), null)).isFalse();
        assertThat(validator.isValid(PageRequest.of(9, 110), null)).isFalse();
    }

    @Test
    void isValid_givenInvalidModulo_thenFalse() {
        assertThat(validator.isValid(PageRequest.of(0, 1), null)).isFalse();
        assertThat(validator.isValid(PageRequest.of(0, 2), null)).isFalse();
        assertThat(validator.isValid(PageRequest.of(0, 5), null)).isFalse();
        assertThat(validator.isValid(PageRequest.of(0, 11), null)).isFalse();
        assertThat(validator.isValid(PageRequest.of(0, 101), null)).isFalse();
        assertThat(validator.isValid(PageRequest.of(0, 202), null)).isFalse();
        assertThat(validator.isValid(PageRequest.of(0, 303), null)).isFalse();
        assertThat(validator.isValid(PageRequest.of(1, 333), null)).isFalse();
    }

}
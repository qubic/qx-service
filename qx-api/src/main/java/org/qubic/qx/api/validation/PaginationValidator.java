package org.qubic.qx.api.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.data.domain.Pageable;

public class PaginationValidator implements ConstraintValidator<Pagination, Pageable> {

    private static final int PAGE_SIZE_MODULO = 10;
    private static final int MAX_RESULT_SIZE = 1000;

    @Override
    public boolean isValid(Pageable value, ConstraintValidatorContext context) {
        if (value == null) {
            return false;
        }
        boolean validPageSize = value.getPageSize() % PAGE_SIZE_MODULO == 0; // we only allow page certain page sizes
        boolean validMaxResultsSize = value.getOffset() + value.getPageSize() <= MAX_RESULT_SIZE;
        return validPageSize && validMaxResultsSize;
    }
}

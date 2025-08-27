package org.qubic.qx.api.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target( { ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PaginationValidator.class)
public @interface Pagination {

    String message() default "Invalid pagination information";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

}


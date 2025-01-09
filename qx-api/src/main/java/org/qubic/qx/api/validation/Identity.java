package org.qubic.qx.api.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = IdentityValidator.class)
public @interface Identity {
    String message() default "invalid identity";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    int[] value() default {};
}

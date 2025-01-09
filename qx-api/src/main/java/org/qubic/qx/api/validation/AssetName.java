package org.qubic.qx.api.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Pattern;

import java.lang.annotation.*;

@Pattern(regexp = "^[A-Z]{1,7}$")
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = {})
public @interface AssetName {
    String message() default "invalid asset name";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    int[] value() default {};
}

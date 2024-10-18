package org.qubic.qx.api.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = OneOfValidator.class)
public @interface OneOf {
    String message() default "value must match one of the allowed values";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    int[] value() default {};
}

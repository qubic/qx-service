package org.qubic.qx.api.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.ArrayUtils;

public class OneOfValidator implements ConstraintValidator<OneOf, Integer> {

    private int[] allowedValues;

    @Override
    public void initialize(OneOf constraintAnnotation) {
        allowedValues = constraintAnnotation.value();
    }

    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext context) {
        return value != null && ArrayUtils.contains(allowedValues, value);
    }

}

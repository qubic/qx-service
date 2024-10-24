package org.qubic.qx.api.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.ArrayUtils;

import java.util.List;

public class AllOfValidator implements ConstraintValidator<AllOf, List<Integer>> {

    int[] allowedValues;

    @Override
    public void initialize(AllOf constraintAnnotation) {
        allowedValues = constraintAnnotation.value();
    }

    @Override
    public boolean isValid(List<Integer> values, ConstraintValidatorContext context) {
        return values == null || values.isEmpty() || values.stream().allMatch(value -> value != null && ArrayUtils.contains(allowedValues, value));
    }

}

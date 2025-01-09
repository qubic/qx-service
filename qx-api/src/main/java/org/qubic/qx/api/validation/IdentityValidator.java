package org.qubic.qx.api.validation;

import at.qubic.api.crypto.IdentityUtil;
import at.qubic.api.crypto.NoCrypto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class IdentityValidator implements ConstraintValidator<Identity, String> {

    private final IdentityUtil identityUtil;

    @SuppressWarnings("unused") // needed for unit test
    IdentityValidator() {
        this(new IdentityUtil(true, new NoCrypto()));
        log.warn("Default constructor used. This should only happen in tests.");
    }

    public IdentityValidator(IdentityUtil identityUtil) {
        this.identityUtil = identityUtil;
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return identityUtil.isValidIdentity(value);
    }

}

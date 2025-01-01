package org.qubic.qx.api.validation;

import at.qubic.api.crypto.IdentityUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.math.BigInteger;
import java.util.Optional;

@Slf4j
public class ValidationUtility {

    private final IdentityUtil identityUtil;

    public ValidationUtility(IdentityUtil identityUtil) {
        this.identityUtil = identityUtil;
    }

    public Optional<ValidationError> validateAmount(BigInteger amount) {
        ValidationError error = null;
        if (amount.signum() <= 0) {
            String message = String.format("Invalid amount [%d].", amount);
            log.warn(message);
            error = new ValidationError(message);
        }
        return Optional.ofNullable(error);
    }

    public Optional<ValidationError> validateIdentity(String identity) {
        ValidationError error = null;
        if (!identityUtil.isValidIdentity(identity)) {
            String message = String.format("Invalid identity [%s]", identity);
            log.warn(message);
            error = new ValidationError(message);
        }
        return Optional.ofNullable(error);
    }

    public Optional<ValidationError> validateAssetName(String name) {
        ValidationError error = null;
        if (StringUtils.isBlank(name) || StringUtils.length(name) > 7) {
            String message = String.format("Invalid asset name [%s]", name);
            log.warn(message);
            error = new ValidationError(message);
        }
        return Optional.ofNullable(error);
    }


}

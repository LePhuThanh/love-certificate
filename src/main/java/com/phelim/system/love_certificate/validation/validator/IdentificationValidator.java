package com.phelim.system.love_certificate.validation.validator;

import com.phelim.system.love_certificate.exception.ErrorCode;
import com.phelim.system.love_certificate.validation.annotation.Identification;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class IdentificationValidator implements ConstraintValidator<Identification, String> {

    private Set<Identification.Type> acceptedTypes;

    @Override
    public void initialize(Identification constraintAnnotation) {
        acceptedTypes = new HashSet<>(Arrays.asList(constraintAnnotation.acceptedTypes()));
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {

        if (value == null || value.trim().isEmpty()) {
            return true; // let @NotBlank handle it
        }

        String normalized = value.replaceAll("\\s+", "");
        boolean isValid = false;

        // CMND: 9 digits
        if (acceptedTypes.contains(Identification.Type.CMND)
                && normalized.matches("^\\d{9}$")) {
            isValid = true;
        }

        // CCCD: 12 digits
        if (acceptedTypes.contains(Identification.Type.CCCD)
                && normalized.matches("^\\d{12}$")) {
            isValid = true;
        }

        if (!isValid) {
            buildMessage(context);
            return false;
        }
        return true;
    }

    private void buildMessage(ConstraintValidatorContext context) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(
                ErrorCode.IDENTIFICATION_INVALID_FORMAT.getMessage()
        ).addConstraintViolation();
    }
}

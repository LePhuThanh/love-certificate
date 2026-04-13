package com.phelim.system.love_certificate.validation.validator;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.phelim.system.love_certificate.exception.ErrorCode;
import com.phelim.system.love_certificate.validation.annotation.PhoneNumber;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PhoneNumberValidator implements ConstraintValidator<PhoneNumber, String> {

    private boolean allowInternational;
    private String region;

    private final PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();

    @Override
    public void initialize(PhoneNumber constraintAnnotation) {
        this.allowInternational = constraintAnnotation.allowInternational();
        this.region = constraintAnnotation.region();
    }

    /**
     * libphonenumber can handle:
     + "0987 123 456" => valid
     +  "+84 987123456" => valid
     + "84987123456" => valid
     + "0123456789" (Old phone number) => invalid
     + Distinguishing between different network providers correctly
     */

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {

        if (value == null || value.trim().isEmpty()) {
            return true; // let @NotBlank handle it
        }

        try {
            Phonenumber.PhoneNumber number = phoneUtil.parse(value, region);

            boolean isValid = phoneUtil.isValidNumber(number);
            if (!allowInternational) {
                boolean isVN = region.equalsIgnoreCase(phoneUtil.getRegionCodeForNumber(number));
                isValid = isValid && isVN;
            }

            if (!isValid) {
                buildMessage(context);
                return false;
            }

            return true;
        } catch (NumberParseException e) {
            buildMessage(context);
            return false;
        }
    }

    private void buildMessage(ConstraintValidatorContext context) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(ErrorCode.PHONE_NUMBER_INVALID_FORMAT.getMessage())
                .addConstraintViolation();
    }
}

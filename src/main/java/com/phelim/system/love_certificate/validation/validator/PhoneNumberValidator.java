package com.phelim.system.love_certificate.validation.validator;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.phelim.system.love_certificate.exception.ErrorCode;
import com.phelim.system.love_certificate.validation.annotation.PhoneNumber;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;

public class PhoneNumberValidator implements ConstraintValidator<PhoneNumber, String> {

    private boolean allowInternational;
    private String region;
    private String[] allowedRegions;

    private final PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();

    @Override
    public void initialize(PhoneNumber constraintAnnotation) {
        this.allowInternational = constraintAnnotation.allowInternational();
        this.region = constraintAnnotation.region();
        this.allowedRegions = constraintAnnotation.allowedRegions();
    }

    /** Cmt by Phelim (14/04/2026) ex: for VN-format
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
            /**
             * "0987..." => requires knowing it's Vietnam
             * "+84987..." => region not required
             */
            // Parse with region fallback
            Phonenumber.PhoneNumber number = phoneUtil.parse(value, region);

            /**
             * Check:
             * Length
             * Valid prefix
             * Existing country
             */
            // Check valid globally
            if (!phoneUtil.isValidNumber(number)) {
                buildMessage(context);
                return false;
            }

            // Get the actual region of the phoneNumber
            String actualRegion = phoneUtil.getRegionCodeForNumber(number);

            // If there is allowedRegions => prioritize checking
            // (@PhoneNumber(allowedRegions = {"VN", "US"}) > @PhoneNumber(allowInternational = true))
            if (allowedRegions != null && allowedRegions.length > 0) {
                boolean match = Arrays.stream(allowedRegions)
                        .anyMatch(r -> r.equalsIgnoreCase(actualRegion));

                if (!match) {
                    buildMessage(context);
                    return false;
                }
                return true;
            }

            // If there isn't allowedRegions => must match region (default)
            if (!allowInternational) {
                if (!actualRegion.equalsIgnoreCase(region)) {
                    buildMessage(context);
                    return false;
                }
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

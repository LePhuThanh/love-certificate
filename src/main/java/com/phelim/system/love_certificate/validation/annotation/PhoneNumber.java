package com.phelim.system.love_certificate.validation.annotation;

import com.phelim.system.love_certificate.validation.validator.PhoneNumberValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/** Cmt by Phelim (14/04/2026)
 * How to use
 *  Case 1: VN only
 * @PhoneNumber

 *  Case 2: US only
 * @PhoneNumber(region = "US")

 *  Case 3: Global permission
 * @PhoneNumber(allowInternational = true)

 *  Case 4: Whitelist
 * @PhoneNumber(allowedRegions = {"VN", "US"})
 */
// Custom annotation
@Documented
@Constraint(validatedBy = PhoneNumberValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER }) // Allowed to be used in: fields in DTOs and parameter methods
@Retention(RetentionPolicy.RUNTIME)
public @interface PhoneNumber {

    String message() default "Invalid phone number format";

    /**
     * Region used for parsing fallback
     * Example: "VN", "US", "KR"
     */
    String region() default "VN";

    /**
     * Allow international phone numbers or not
     * true -> accept all countries
     * false -> only accept the correct region
     */
    boolean allowInternational() default false;

    /**
     * List of permitted regions (regions are prioritized)
     */
    String[] allowedRegions() default {};

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
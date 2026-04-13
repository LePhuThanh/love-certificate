package com.phelim.system.love_certificate.validation.annotation;

import com.phelim.system.love_certificate.validation.validator.PhoneNumberValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

// Custom annotation
@Documented
@Constraint(validatedBy = PhoneNumberValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER }) // Allowed to be used in: fields in DTOs and parameter methods
@Retention(RetentionPolicy.RUNTIME)
public @interface PhoneNumber {

    String message() default "Invalid phone number format";

    String region() default "VN"; // default is Vietnam

    boolean allowInternational() default false; // Config custom

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
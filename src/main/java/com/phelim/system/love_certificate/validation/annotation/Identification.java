package com.phelim.system.love_certificate.validation.annotation;

import com.phelim.system.love_certificate.validation.validator.IdentificationValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

// Custom annotation
@Documented
@Constraint(validatedBy = IdentificationValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface Identification {

    String message() default "Định dạng CCCD/CMND không hợp lệ";

    /**
     * Which types of documents are accepted:
     * National ID Card (9 digits)
     * Citizen Identification Card (12 digits)
     */
    Type[] acceptedTypes() default { Type.CMND, Type.CCCD };

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    enum Type {
        CMND, // 9 digits
        CCCD  // 12 digits
    }
}

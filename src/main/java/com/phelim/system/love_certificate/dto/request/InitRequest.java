package com.phelim.system.love_certificate.dto.request;

import com.phelim.system.love_certificate.dto.HasRequestId;
import jakarta.persistence.Column;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InitRequest implements HasRequestId {
    @NotBlank(message = "requestId is required")
    private String requestId;
    @NotBlank(message = "maleName is required")
    private String maleName;
    @NotBlank(message = "femaleName is required")
    private String femaleName;

    private Integer maleAge;
    private Integer femaleAge;
    @NotNull(message = "loveStartDate is required")
    private LocalDate loveStartDate;

    //    @Pattern(
//            regexp = "^[A-Za-z0-9+_.-]+@(.+)$",
//            message = "Invalid email format"
//    )
    @NotBlank(message = "email is required")
    @Email(message = "Invalid email format")
    private String email;
}

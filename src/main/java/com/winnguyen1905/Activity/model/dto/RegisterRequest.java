package com.winnguyen1905.Activity.model.dto;

import com.winnguyen1905.Activity.common.constant.AccountRole;
import com.winnguyen1905.Activity.common.constant.MajorType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
    @NotBlank(message = "username not be blank") @Pattern(regexp = "^[a-zA-Z0-9]{8,20}$", message = "username must be of 8 to 20 length with no special characters") String identifyCode,

    @NotBlank @Size(min = 8, message = "The password must be length >= 8") String password,

    @NotBlank @Email(message = "Email format invalid") String email,

    String phone,

    MajorType major,

    @NotBlank String fullName,

    AccountRole role

) implements AbstractModel {
}

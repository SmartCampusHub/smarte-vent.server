package com.winnguyen1905.activity.model.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest implements AbstractModel {
    @Pattern(regexp = "^[a-zA-Z0-9]{8,20}$", message = "username must be of 8 to 20 length with no special characters")
    private String identifyCode;

    @NotBlank(message = "password invalid")
    @Size(min = 8, max = 20, message = "password's length must >= 8")
    private String password;
}

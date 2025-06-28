package com.winnguyen1905.activity.model.dto;

import com.winnguyen1905.activity.common.constant.AccountRole;
import com.winnguyen1905.activity.common.constant.MajorType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest implements AbstractModel {
    @NotBlank(message = "username not be blank") 
    @Pattern(regexp = "^[a-zA-Z0-9]{8,20}$", message = "username must be of 8 to 20 length with no special characters") 
    private String identifyCode;

    @NotBlank 
    @Size(min = 8, message = "The password must be length >= 8") 
    private String password;

    @NotBlank 
    @Email(message = "Email format invalid") 
    private String email;

    private String phone;

    private MajorType major;

    @NotBlank 
    private String fullName;

    private AccountRole role;
}

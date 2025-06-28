package com.winnguyen1905.activity.model.dto;

import com.winnguyen1905.activity.common.constant.AccountRole;
import com.winnguyen1905.activity.common.constant.MajorType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUpdateAccount implements AbstractModel {
    @NotBlank(message = "Full name is required") 
    @Size(max = 100, message = "Full name must be less than 100 characters") 
    private String fullName;
    
    private String email;
    private String phone;
    private MajorType major;
    private AccountRole role;
    private Boolean isActive;
}

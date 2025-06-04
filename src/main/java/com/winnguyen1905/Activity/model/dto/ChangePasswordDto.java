package com.winnguyen1905.Activity.model.dto;

import lombok.Data;

@Data
public class ChangePasswordDto implements AbstractModel {
    private String oldPassword;
    private String newPassword;
    private String confirmPassword;
}

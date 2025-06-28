package com.winnguyen1905.activity.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAccountDto implements AbstractModel {
    private Long id;
    private String phone;
    private String email;
}

package com.winnguyen1905.activity.model.viewmodel;

import java.util.UUID;

import com.winnguyen1905.activity.common.constant.AccountRole;
import com.winnguyen1905.activity.common.constant.MajorType;
import com.winnguyen1905.activity.model.dto.AbstractModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountVm implements AbstractModel {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String identifyCode;
    private MajorType major;
    private Boolean isActive;
    private AccountRole role;
}

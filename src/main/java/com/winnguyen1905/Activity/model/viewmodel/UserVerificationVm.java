package com.winnguyen1905.activity.model.viewmodel;

import java.util.UUID;
import com.winnguyen1905.activity.model.dto.AbstractModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserVerificationVm implements AbstractModel {
    private UUID id;
    private String verificationCode;
}

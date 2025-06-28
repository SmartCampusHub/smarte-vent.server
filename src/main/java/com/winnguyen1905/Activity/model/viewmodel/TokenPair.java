package com.winnguyen1905.activity.model.viewmodel;

import com.winnguyen1905.activity.model.dto.AbstractModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenPair implements AbstractModel {
    private String accessToken;
    private String refreshToken;
}

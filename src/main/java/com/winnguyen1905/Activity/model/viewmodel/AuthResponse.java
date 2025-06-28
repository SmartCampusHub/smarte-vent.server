package com.winnguyen1905.activity.model.viewmodel;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.winnguyen1905.activity.model.dto.AbstractModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse implements AbstractModel {
    private String accessToken;
    private AccountVm account;
    @JsonIgnore
    private String refreshToken;
}

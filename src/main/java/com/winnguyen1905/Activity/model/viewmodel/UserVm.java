package com.winnguyen1905.activity.model.viewmodel;

import java.util.List;
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
public class UserVm implements AbstractModel {
    private UUID id;
    private String email;
    private String username;
    private String avatarUrl;
    private List<String> roles;
}

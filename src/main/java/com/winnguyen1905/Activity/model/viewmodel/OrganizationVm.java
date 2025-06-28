package com.winnguyen1905.activity.model.viewmodel;

import com.winnguyen1905.activity.common.constant.OrganizationType;
import com.winnguyen1905.activity.model.dto.AbstractModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationVm implements AbstractModel {
    private Long id;
    private String organizationName;
    private String representativePhone;
    private String representativeEmail;
    private OrganizationType organizationType;
}

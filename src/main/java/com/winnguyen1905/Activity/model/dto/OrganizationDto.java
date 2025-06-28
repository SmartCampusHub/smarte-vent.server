package com.winnguyen1905.activity.model.dto;

import com.winnguyen1905.activity.common.constant.OrganizationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationDto implements AbstractModel {
    private Long id;
    private String organizationName;
    private String representativePhone;
    private String representativeEmail;
    private OrganizationType organizationType;
}

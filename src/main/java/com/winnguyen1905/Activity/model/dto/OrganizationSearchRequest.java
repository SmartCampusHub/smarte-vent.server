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
public class OrganizationSearchRequest implements AbstractModel {
    private String name;
    private OrganizationType organizationType;
}

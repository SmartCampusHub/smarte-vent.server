package com.winnguyen1905.Activity.model.dto;

import com.winnguyen1905.Activity.common.constant.OrganizationType;

public record OrganizationSearchRequest(String name, OrganizationType organizationType) implements AbstractModel  {

}

package com.winnguyen1905.Activity.model.viewmodel;

import com.winnguyen1905.Activity.common.constant.OrganizationType;
import com.winnguyen1905.Activity.model.dto.AbstractModel;

import lombok.Builder;

public record OrganizationVm(
    Long id,
    String organizationName,
    String representativePhone,
    String representativeEmail,
    OrganizationType organizationType) implements AbstractModel {
      @Builder
      public OrganizationVm(
          Long id,
          String organizationName,
          String representativePhone,
          String representativeEmail,
          OrganizationType organizationType) {
        this.id = id;
        this.organizationName = organizationName;
        this.representativePhone = representativePhone;
        this.representativeEmail = representativeEmail;
        this.organizationType = organizationType;
      }
}

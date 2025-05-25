package com.winnguyen1905.Activity.model.dto;

import com.winnguyen1905.Activity.common.constant.OrganizationType;

import lombok.Builder;

public record OrganizationDto(
    Long id,
    String organizationName,
    String representativePhone,
    String representativeEmail,
    OrganizationType organizationType) implements AbstractModel {
  @Builder
  public OrganizationDto(
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

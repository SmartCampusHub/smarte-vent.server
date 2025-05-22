package com.winnguyen1905.Activity.model.viewmodel;

import com.winnguyen1905.Activity.model.dto.AbstractModel;

import lombok.Builder;

public record RepresentativeOrganizerVm(
    Long id,
    String organizationName,
    String representativeName,
    String representativeEmail,
    String representativePhone,
    String representativePosition) implements AbstractModel {
  @Builder
  public RepresentativeOrganizerVm(
      Long id,
      String organizationName,
      String representativeName,
      String representativeEmail,
      String representativePhone,
      String representativePosition) {
    this.id = id;
    this.organizationName = organizationName;
    this.representativeName = representativeName;
    this.representativeEmail = representativeEmail;
    this.representativePhone = representativePhone;
    this.representativePosition = representativePosition;
  }
}

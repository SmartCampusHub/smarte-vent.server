package com.winnguyen1905.Activity.model.viewmodel;

import lombok.Builder;

@Builder
public record RepresentativeOrganizerVm(
    Long id,
    String organizationName,
    String representativeName,
    String representativeEmail,
    String representativePhone,
    String representativePosition
) {} 

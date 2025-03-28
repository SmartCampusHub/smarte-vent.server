package com.winnguyen1905.Activity.rest.service;

import com.winnguyen1905.Activity.common.annotation.TAccountRequest;
import com.winnguyen1905.Activity.model.dto.RepresentativeOrganizerDto;
import com.winnguyen1905.Activity.model.viewmodel.RepresentativeOrganizerVm;

import java.util.List;

public interface RepresentativeOrganizerService {
    void createOrganizer(TAccountRequest accountRequest, RepresentativeOrganizerDto organizerDto);
    void updateOrganizer(TAccountRequest accountRequest, RepresentativeOrganizerDto organizerDto, Long id);
    void deleteOrganizer(TAccountRequest accountRequest, Long id);
    RepresentativeOrganizerVm getOrganizerById(Long id);
    List<RepresentativeOrganizerVm> getAllOrganizers();
}

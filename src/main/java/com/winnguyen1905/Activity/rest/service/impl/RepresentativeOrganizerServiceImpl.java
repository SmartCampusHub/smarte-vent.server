package com.winnguyen1905.Activity.rest.service.impl;

import com.winnguyen1905.Activity.common.annotation.TAccountRequest;
import com.winnguyen1905.Activity.model.dto.RepresentativeOrganizerDto;
import com.winnguyen1905.Activity.model.viewmodel.RepresentativeOrganizerVm;
import com.winnguyen1905.Activity.persistance.repository.RepresentativeOrganizerRepository;
import com.winnguyen1905.Activity.rest.service.RepresentativeOrganizerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RepresentativeOrganizerServiceImpl implements RepresentativeOrganizerService {
    private final RepresentativeOrganizerRepository organizerRepository;

    @Override
    public void createOrganizer(TAccountRequest accountRequest, RepresentativeOrganizerDto organizerDto) {
        // TODO: Implement create organizer logic
    }

    @Override
    public void updateOrganizer(TAccountRequest accountRequest, RepresentativeOrganizerDto organizerDto, Long id) {
        // TODO: Implement update organizer logic
    }

    @Override
    public void deleteOrganizer(TAccountRequest accountRequest, Long id) {
        // TODO: Implement delete organizer logic
    }

    @Override
    public RepresentativeOrganizerVm getOrganizerById(Long id) {
        // TODO: Implement get organizer by id logic
        return null;
    }

    @Override
    public List<RepresentativeOrganizerVm> getAllOrganizers() {
        // TODO: Implement get all organizers logic
        return null;
    }
}

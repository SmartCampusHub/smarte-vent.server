package com.winnguyen1905.activity.rest.service;

import com.winnguyen1905.activity.common.annotation.TAccountRequest;
import com.winnguyen1905.activity.model.dto.OrganizationDto;
import com.winnguyen1905.activity.model.dto.OrganizationSearchRequest;
import com.winnguyen1905.activity.model.viewmodel.OrganizationVm;
import com.winnguyen1905.activity.model.viewmodel.PagedResponse;
import com.winnguyen1905.activity.model.viewmodel.RepresentativeOrganizerVm;

import java.util.List;

import org.aspectj.weaver.ast.Or;
import org.springframework.data.domain.Pageable;

public interface OrganizerService {
    void createOrganizer(TAccountRequest accountRequest, OrganizationDto organizerDto);
    void updateOrganizer(TAccountRequest accountRequest, OrganizationDto organizerDto);
    void deleteOrganizer(TAccountRequest accountRequest, Long id);

    PagedResponse<OrganizationVm> getAllOrganizers(OrganizationSearchRequest organizationSearchRequest, Pageable pageable);
    OrganizationVm getOrganizerById(Long id);
    void deleteOrganizerById(Long id);
    PagedResponse<OrganizationVm> getAllOrganizersByPage(int page, int size);
}

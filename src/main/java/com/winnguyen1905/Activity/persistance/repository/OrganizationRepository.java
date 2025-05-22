package com.winnguyen1905.Activity.persistance.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.winnguyen1905.Activity.persistance.entity.EOrganization;

public interface OrganizationRepository extends JpaRepository<EOrganization, Long> {
}

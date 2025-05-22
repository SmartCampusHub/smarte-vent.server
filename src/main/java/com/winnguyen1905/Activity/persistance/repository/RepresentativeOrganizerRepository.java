package com.winnguyen1905.Activity.persistance.repository;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.winnguyen1905.Activity.persistance.entity.EOrganization;

@Repository
public interface RepresentativeOrganizerRepository
    extends JpaRepository<EOrganization, Long>, JpaSpecificationExecutor<EOrganization> {
  // Custom query methods can be defined here if needed
  // For example, you can add methods to find organizers by specific criteria
  // or to perform complex queries using the Specification interface.
}

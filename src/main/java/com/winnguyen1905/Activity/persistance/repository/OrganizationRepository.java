package com.winnguyen1905.activity.persistance.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.winnguyen1905.activity.persistance.entity.EOrganization;

@Repository
public interface OrganizationRepository extends JpaRepository<EOrganization, Long> {
}

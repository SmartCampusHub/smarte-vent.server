package com.winnguyen1905.Activity.persistance.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.winnguyen1905.Activity.persistance.entity.ERepresentativeOrganizer;

@Repository
public interface RepresentativeOrganizerRepository extends JpaRepository<ERepresentativeOrganizer, Long> {
} 

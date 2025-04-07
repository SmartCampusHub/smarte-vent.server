package com.winnguyen1905.Activity.persistance.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.winnguyen1905.Activity.persistance.entity.EStudentSemesterDetail;

@Repository
public interface StudentSemesterDetailRepository extends JpaRepository<EStudentSemesterDetail, Long> {
  List<EStudentSemesterDetail> findAllByStudentIdOrderBySemesterNumber(Long id);
} 

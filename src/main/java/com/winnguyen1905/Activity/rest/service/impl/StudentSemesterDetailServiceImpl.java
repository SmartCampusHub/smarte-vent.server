package com.winnguyen1905.Activity.rest.service.impl;

import com.winnguyen1905.Activity.common.annotation.TAccountRequest;
import com.winnguyen1905.Activity.model.dto.StudentSemesterDetailDto;
import com.winnguyen1905.Activity.model.viewmodel.StudentSemesterDetailVm;
import com.winnguyen1905.Activity.persistance.entity.EAccountCredentials;
import com.winnguyen1905.Activity.persistance.entity.EStudentSemesterDetail;
import com.winnguyen1905.Activity.persistance.repository.AccountRepository;
import com.winnguyen1905.Activity.persistance.repository.StudentSemesterDetailRepository;
import com.winnguyen1905.Activity.rest.service.StudentSemesterDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudentSemesterDetailServiceImpl implements StudentSemesterDetailService {

  private final AccountRepository accountRepository;
  private final StudentSemesterDetailRepository studentSemesterDetailRepository;

  @Override
  public List<StudentSemesterDetailVm> getAllSemesterDetails(TAccountRequest accountRequest) {
    List<EStudentSemesterDetail> studentSemesterDetails = this.studentSemesterDetailRepository
        .findAllByStudentId(accountRequest.id());
    return studentSemesterDetails.stream()
        .map(detail -> StudentSemesterDetailVm.builder()
            .id(detail.getId())
            .studentId(detail.getStudent().getId())
            .attendanceScore(detail.getAttendanceScore())
            .gpa(detail.getGpa())
            .build())
        .collect(Collectors.toList());
  }

  @Override
  public StudentSemesterDetailVm createDetail(TAccountRequest accountRequest, StudentSemesterDetailDto detailDto) {
    EAccountCredentials student = this.accountRepository.findById(detailDto.studentId())
        .orElseThrow(() -> new IllegalArgumentException("Student not found with ID: " + detailDto.studentId()));

    EStudentSemesterDetail newDetail = EStudentSemesterDetail.builder()
        .student(student)
        .attendanceScore(detailDto.attendanceScore())
        .gpa(detailDto.gpa())
        .createdBy(accountRequest.username())
        .updatedBy(accountRequest.username())
        .build();

    EStudentSemesterDetail savedDetail = studentSemesterDetailRepository.save(newDetail);
    return mapToViewModel(savedDetail);
  }

  @Override
  public StudentSemesterDetailVm updateDetail(TAccountRequest accountRequest, StudentSemesterDetailDto detailDto) {
    Optional<EStudentSemesterDetail> existingDetailOpt = studentSemesterDetailRepository.findById(detailDto.id());
    if (existingDetailOpt.isEmpty()) {
      throw new IllegalArgumentException("Student semester detail not found with ID: " + detailDto.id());
    }

    EStudentSemesterDetail existingDetail = existingDetailOpt.get();
    existingDetail.setAttendanceScore(detailDto.attendanceScore());
    existingDetail.setGpa(detailDto.gpa());
    existingDetail.setUpdatedBy(accountRequest.username());

    EStudentSemesterDetail updatedDetail = studentSemesterDetailRepository.save(existingDetail);
    return mapToViewModel(updatedDetail);
  }

  @Override
  public void deleteDetail(TAccountRequest accountRequest, Long id) {
    if (!studentSemesterDetailRepository.existsById(id)) {
      throw new IllegalArgumentException("Student semester detail not found with ID: " + id);
    }
    studentSemesterDetailRepository.deleteById(id);
  }

  @Override
  public StudentSemesterDetailVm getDetailById(Long id) {
    EStudentSemesterDetail detail = studentSemesterDetailRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Student semester detail not found with ID: " + id));
    return mapToViewModel(detail);
  }

  @Override
  public List<StudentSemesterDetailVm> getDetailsByStudentId(Long studentId) {
    List<EStudentSemesterDetail> studentSemesterDetails = studentSemesterDetailRepository.findAllByStudentId(studentId);
    return studentSemesterDetails.stream()
        .map(this::mapToViewModel)
        .collect(Collectors.toList());
  }

  
  private StudentSemesterDetailVm mapToViewModel(EStudentSemesterDetail detail) {
    return StudentSemesterDetailVm.builder()
        .id(detail.getId())
        .studentId(detail.getStudent().getId())
        .attendanceScore(detail.getAttendanceScore().intValue())
        .gpa(detail.getGpa().floatValue())
        .build();
  }
}

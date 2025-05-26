package com.winnguyen1905.Activity.rest.service.impl;

import com.winnguyen1905.Activity.common.annotation.TAccountRequest;
import com.winnguyen1905.Activity.common.constant.ParticipationStatus;
import com.winnguyen1905.Activity.model.dto.StudentSemesterDetailDto;
import com.winnguyen1905.Activity.model.viewmodel.StudentSemesterDetailVm;
import com.winnguyen1905.Activity.persistance.entity.EAccountCredentials;
import com.winnguyen1905.Activity.persistance.entity.EParticipationDetail;
import com.winnguyen1905.Activity.persistance.repository.AccountRepository;
import com.winnguyen1905.Activity.persistance.repository.ParticipationDetailRepository;
import com.winnguyen1905.Activity.persistance.repository.StudentSemesterDetailRepository;
import com.winnguyen1905.Activity.rest.service.StudentSemesterDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudentSemesterDetailServiceImpl {

  // private final AccountRepository accountRepository;
  // private final ParticipationDetailRepository participationDetailRepository;
  // private final StudentSemesterDetailRepository studentSemesterDetailRepository;

  // @Override
  // public List<StudentSemesterDetailVm> getAllSemesterDetails(TAccountRequest accountRequest) {

  //   List<EStudentSemesterDetail> studentSemesterDetails = this.studentSemesterDetailRepository
  //       .findAllByStudentIdOrderBySemesterNumber(accountRequest.id());

  //   List<EParticipationDetail> participationDetails = participationDetailRepository
  //       .findVerifiedSpecificParticipationDetailsWithinDateRange(
  //           studentSemesterDetails.getFirst().getStartDate(),
  //           studentSemesterDetails.getLast().getEndDate(), ParticipationStatus.VERIFIED, accountRequest.id());

  //   List<StudentSemesterDetailVm> studentSemesterDetailVms = new ArrayList<>();

  //   for (EStudentSemesterDetail detail : studentSemesterDetails) {
  //     Integer collector = 0;
  //     for (EParticipationDetail participationDetail : participationDetails) {
  //       if (participationDetail.getActivity().getStartDate().isAfter(detail.getStartDate())
  //           && participationDetail.getActivity().getEndDate()
  //               .isBefore(detail.getEndDate())) {
  //         collector += participationDetail.getActivity().getAttendanceScoreUnit();
  //       }
  //     }
  //     studentSemesterDetailVms.add(StudentSemesterDetailVm.builder()
  //         .id(detail.getId())
  //         .semesterNumber(detail.getSemesterNumber())
  //         .semesterYear(detail.getSemesterYear())
  //         .studentId(detail.getStudent().getId())
  //         .attendanceScore(detail.getAttendanceScore())
  //         .attendanceScoreFromActivity(collector)
  //         .gpa(detail.getGpa())
  //         .build());
  //   }

  //   return studentSemesterDetailVms;
  // }

  // @Override
  // public StudentSemesterDetailVm createDetail(TAccountRequest accountRequest, StudentSemesterDetailDto detailDto) {
  //   EAccountCredentials student = this.accountRepository.findById(detailDto.studentId())
  //       .orElseThrow(() -> new IllegalArgumentException("Student not found with ID: " + detailDto.studentId()));

  //   EStudentSemesterDetail newDetail = EStudentSemesterDetail.builder()
  //       .student(student)
  //       .attendanceScore(detailDto.attendanceScore())
  //       .gpa(detailDto.gpa())
  //       .createdBy(accountRequest.username())
  //       .updatedBy(accountRequest.username())
  //       .build();

  //   EStudentSemesterDetail savedDetail = studentSemesterDetailRepository.save(newDetail);
  //   return mapToViewModel(savedDetail);
  // }

  // @Override
  // public StudentSemesterDetailVm updateDetail(TAccountRequest accountRequest, StudentSemesterDetailDto detailDto) {
  //   Optional<EStudentSemesterDetail> existingDetailOpt = studentSemesterDetailRepository.findById(detailDto.id());
  //   if (existingDetailOpt.isEmpty()) {
  //     throw new IllegalArgumentException("Student semester detail not found with ID: " + detailDto.id());
  //   }

  //   EStudentSemesterDetail existingDetail = existingDetailOpt.get();
  //   existingDetail.setAttendanceScore(detailDto.attendanceScore());
  //   existingDetail.setGpa(detailDto.gpa());
  //   existingDetail.setUpdatedBy(accountRequest.username());

  //   EStudentSemesterDetail updatedDetail = studentSemesterDetailRepository.save(existingDetail);
  //   return mapToViewModel(updatedDetail);
  // }

  // @Override
  // public void deleteDetail(TAccountRequest accountRequest, Long id) {
  //   if (!studentSemesterDetailRepository.existsById(id)) {
  //     throw new IllegalArgumentException("Student semester detail not found with ID: " + id);
  //   }
  //   studentSemesterDetailRepository.deleteById(id);
  // }

  // @Override
  // public StudentSemesterDetailVm getDetailById(Long id) {
  //   EStudentSemesterDetail detail = studentSemesterDetailRepository.findById(id)
  //       .orElseThrow(() -> new IllegalArgumentException("Student semester detail not found with ID: " + id));
  //   return mapToViewModel(detail);
  // }

  // @Override
  // public List<StudentSemesterDetailVm> getDetailsByStudentId(Long studentId) {
  //   // List<EStudentSemesterDetail> studentSemesterDetails =
  //   // studentSemesterDetailRepository.findAllByStudentId(studentId);
  //   return null;
  // }

  // private StudentSemesterDetailVm mapToViewModel(EStudentSemesterDetail detail) {
  //   return StudentSemesterDetailVm.builder()
  //       .id(detail.getId())
  //       .studentId(detail.getStudent().getId())
  //       .attendanceScore(detail.getAttendanceScore().intValue())
  //       .gpa(detail.getGpa().floatValue())
  //       .build();
  // }
}

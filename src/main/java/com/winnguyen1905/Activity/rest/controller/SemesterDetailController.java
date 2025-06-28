package com.winnguyen1905.activity.rest.controller;

import com.winnguyen1905.activity.common.annotation.AccountRequest;
import com.winnguyen1905.activity.common.annotation.TAccountRequest;
import com.winnguyen1905.activity.model.dto.StudentSemesterDetailDto;
import com.winnguyen1905.activity.model.viewmodel.StudentSemesterDetailVm;
import com.winnguyen1905.activity.rest.service.StudentSemesterDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/semester-details")
@RequiredArgsConstructor
public class SemesterDetailController {

  // private final StudentSemesterDetailService studentSemesterDetailService;

  // @GetMapping
  // public ResponseEntity<List<StudentSemesterDetailVm>> getAllSemesterDetails(@AccountRequest TAccountRequest accountRequest) {
  //   List<StudentSemesterDetailVm> semesterDetails = studentSemesterDetailService.getAllSemesterDetails(accountRequest);
  //   return ResponseEntity.ok(semesterDetails);
  // }

  // @GetMapping("/{id}/detail")
  // public ResponseEntity<StudentSemesterDetailVm> getDetailById(@PathVariable Long id) {
  //   StudentSemesterDetailVm detail = studentSemesterDetailService.getDetailById(id);
  //   return ResponseEntity.ok(detail);
  // }

  // @GetMapping("/student/{studentId}")
  // public ResponseEntity<List<StudentSemesterDetailVm>> getDetailsByStudentId(@PathVariable Long studentId) {
  //   List<StudentSemesterDetailVm> details = studentSemesterDetailService.getDetailsByStudentId(studentId);
  //   return ResponseEntity.ok(details);
  // }

  // @PostMapping
  // public ResponseEntity<StudentSemesterDetailVm> createDetail(@AccountRequest TAccountRequest accountRequest,
  //     @RequestBody StudentSemesterDetailDto detailDto) {
  //   StudentSemesterDetailVm createdDetail = studentSemesterDetailService.createDetail(accountRequest, detailDto);
  //   return ResponseEntity.ok(createdDetail);
  // }

  // @PutMapping("/{id}")
  // public ResponseEntity<StudentSemesterDetailVm> updateDetail(@AccountRequest TAccountRequest accountRequest,
  //     @PathVariable Long id,
  //     @RequestBody StudentSemesterDetailDto detailDto) {
  //   detailDto = new StudentSemesterDetailDto(id, detailDto.studentId(), detailDto.classId(),
  //       detailDto.attendanceScore(), detailDto.gpa());
  //   StudentSemesterDetailVm updatedDetail = studentSemesterDetailService.updateDetail(accountRequest, detailDto);
  //   return ResponseEntity.ok(updatedDetail);
  // }

  // @DeleteMapping("/{id}")
  // public ResponseEntity<Void> deleteDetail(@RequestAttribute TAccountRequest accountRequest, @PathVariable Long id) {
  //   studentSemesterDetailService.deleteDetail(accountRequest, id);
  //   return ResponseEntity.noContent().build();
  // }
}

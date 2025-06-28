package com.winnguyen1905.activity.rest.service;

import com.winnguyen1905.activity.common.annotation.TAccountRequest;
import com.winnguyen1905.activity.model.dto.StudentSemesterDetailDto;
import com.winnguyen1905.activity.model.viewmodel.StudentSemesterDetailVm;

import java.util.List;

public interface StudentSemesterDetailService {
  StudentSemesterDetailVm createDetail(TAccountRequest accountRequest, StudentSemesterDetailDto detailDto);
  StudentSemesterDetailVm updateDetail(TAccountRequest accountRequest, StudentSemesterDetailDto detailDto);
  void deleteDetail(TAccountRequest accountRequest, Long id);
  StudentSemesterDetailVm getDetailById(Long id);
  List<StudentSemesterDetailVm> getAllSemesterDetails(TAccountRequest accountRequest);
  List<StudentSemesterDetailVm> getDetailsByStudentId(Long studentId);
}

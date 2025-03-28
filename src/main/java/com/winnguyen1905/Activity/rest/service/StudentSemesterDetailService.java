package com.winnguyen1905.Activity.rest.service;

import com.winnguyen1905.Activity.common.annotation.TAccountRequest;
import com.winnguyen1905.Activity.model.dto.StudentSemesterDetailDto;
import com.winnguyen1905.Activity.model.viewmodel.StudentSemesterDetailVm;

import java.util.List;

public interface StudentSemesterDetailService {
    void createDetail(TAccountRequest accountRequest, StudentSemesterDetailDto detailDto);
    void updateDetail(TAccountRequest accountRequest, StudentSemesterDetailDto detailDto, Long id);
    void deleteDetail(TAccountRequest accountRequest, Long id);
    StudentSemesterDetailVm getDetailById(Long id);
    List<StudentSemesterDetailVm> getDetailsByStudentId(Long studentId);
}

package com.winnguyen1905.Activity.rest.service.impl;

import com.winnguyen1905.Activity.common.annotation.TAccountRequest;
import com.winnguyen1905.Activity.model.dto.StudentSemesterDetailDto;
import com.winnguyen1905.Activity.model.viewmodel.StudentSemesterDetailVm;
import com.winnguyen1905.Activity.persistance.repository.StudentSemesterDetailRepository;
import com.winnguyen1905.Activity.rest.service.StudentSemesterDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentSemesterDetailServiceImpl implements StudentSemesterDetailService {
    private final StudentSemesterDetailRepository detailRepository;

    @Override
    public void createDetail(TAccountRequest accountRequest, StudentSemesterDetailDto detailDto) {
        // TODO: Implement create detail logic
    }

    @Override
    public void updateDetail(TAccountRequest accountRequest, StudentSemesterDetailDto detailDto, Long id) {
        // TODO: Implement update detail logic
    }

    @Override
    public void deleteDetail(TAccountRequest accountRequest, Long id) {
        // TODO: Implement delete detail logic
    }

    @Override
    public StudentSemesterDetailVm getDetailById(Long id) {
        // TODO: Implement get detail by id logic
        return null;
    }

    @Override
    public List<StudentSemesterDetailVm> getDetailsByStudentId(Long studentId) {
        // TODO: Implement get details by student id logic
        return null;
    }
}

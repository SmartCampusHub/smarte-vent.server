package com.winnguyen1905.Activity.rest.service;

import com.winnguyen1905.Activity.common.annotation.TAccountRequest;
import com.winnguyen1905.Activity.model.dto.ClassDto;
import com.winnguyen1905.Activity.model.viewmodel.ClassVm;
import com.winnguyen1905.Activity.model.viewmodel.PagedResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ClassService {
    void createClass(TAccountRequest accountRequest, ClassDto classDto);
    void updateClass(TAccountRequest accountRequest, ClassDto classDto, Long id);
    void deleteClass(TAccountRequest accountRequest, Long id);
    ClassVm getClassById(Long id);
    PagedResponse<ClassVm> getAllClasses(Pageable pageable);
 //   PagedResponse<List<ClassVm>> getClassesByDepartment(String department);
}

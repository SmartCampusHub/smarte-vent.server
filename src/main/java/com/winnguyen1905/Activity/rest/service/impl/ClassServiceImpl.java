package com.winnguyen1905.Activity.rest.service.impl;

import com.winnguyen1905.Activity.common.annotation.TAccountRequest;
import com.winnguyen1905.Activity.model.dto.ClassDto;
import com.winnguyen1905.Activity.model.viewmodel.ClassVm;
import com.winnguyen1905.Activity.persistance.repository.ClassRepository;
import com.winnguyen1905.Activity.rest.service.ClassService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClassServiceImpl implements ClassService {
    private final ClassRepository classRepository;

    @Override
    public void createClass(TAccountRequest accountRequest, ClassDto classDto) {
        // TODO: Implement create class logic
    }

    @Override
    public void updateClass(TAccountRequest accountRequest, ClassDto classDto, Long id) {
        // TODO: Implement update class logic
    }

    @Override
    public void deleteClass(TAccountRequest accountRequest, Long id) {
        // TODO: Implement delete class logic
    }

    @Override
    public ClassVm getClassById(Long id) {
        // TODO: Implement get class by id logic
        return null;
    }

    @Override
    public List<ClassVm> getAllClasses() {
        // TODO: Implement get all classes logic
        return null;
    }

    @Override
    public List<ClassVm> getClassesByDepartment(String department) {
        // TODO: Implement get classes by department logic
        return null;
    }
}

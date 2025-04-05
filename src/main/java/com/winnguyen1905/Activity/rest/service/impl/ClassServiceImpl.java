package com.winnguyen1905.Activity.rest.service.impl;

import com.winnguyen1905.Activity.common.annotation.TAccountRequest;
import com.winnguyen1905.Activity.common.constant.AccountRole;
import com.winnguyen1905.Activity.common.constant.ClassStatus;
import com.winnguyen1905.Activity.exception.BadRequestException;
import com.winnguyen1905.Activity.model.dto.ClassDto;
import com.winnguyen1905.Activity.model.dto.RegisterRequest;
import com.winnguyen1905.Activity.model.viewmodel.ClassVm;
import com.winnguyen1905.Activity.model.viewmodel.PagedResponse;
import com.winnguyen1905.Activity.persistance.entity.EAccountCredentials;
import com.winnguyen1905.Activity.persistance.entity.EClass;
import com.winnguyen1905.Activity.persistance.repository.AccountRepository;
import com.winnguyen1905.Activity.persistance.repository.ClassRepository;
import com.winnguyen1905.Activity.rest.service.ClassService;
import jakarta.validation.constraints.Null;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ClassServiceImpl implements ClassService {

  private final ClassRepository classRepository;
  private final AccountRepository accountRepository;

  @Override
  public void createClass(TAccountRequest accountRequest, ClassDto classDto) {
    // validateClassDto(classDto);
    // validateAccountRequest(accountRequest);

    EClass eClass = EClass.builder()
        .className(classDto.getClassName())
        .academicYear(classDto.getAcademicYear())
        .startDate(classDto.getStartDate())
        .endDate(classDto.getEndDate())
        .department(classDto.getDepartment())
        .capacity(classDto.getCapacity())
        .status(classDto.getStatus())
        .createdBy(accountRequest.username())
        .build();
    classRepository.save(eClass);

    // If students are provided, set them
    // if (classDto.getStudent() != null || !classDto.getStudent().isEmpty()) {
    // for (RegisterRequest request : classDto.getStudent()) {
    // EAccountCredentials student =
    // accountRepository.findByStudentCode(request.studentCode())
    // .orElseThrow(() -> new IllegalArgumentException("Student with code " +
    // request.studentCode() + " not found"));
    //
    // if (student.getRole() != AccountRole.STUDENT) {
    // throw new IllegalArgumentException("Account with code " +
    // request.studentCode() + " is not a student");
    // }
    // student.setStudentClass(eClass);
    // }
    // }
  }

  @Override
  public void updateClass(TAccountRequest accountRequest, ClassDto classDto, Long id) {
    validateAccountRequest(accountRequest);

    EClass existingClass = classRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Class with ID " + id + " not found"));

    existingClass.setClassName(classDto.getClassName());
    existingClass.setAcademicYear(classDto.getAcademicYear());
    existingClass.setStartDate(classDto.getStartDate());
    existingClass.setEndDate(classDto.getEndDate());
    existingClass.setDepartment(classDto.getDepartment());
    existingClass.setCapacity(classDto.getCapacity());
    existingClass.setUpdatedBy(accountRequest.username());
    classRepository.save(existingClass);

    // Update Student if provided
    // if (classDto.getStudent() != null || !classDto.getStudent().isEmpty()) {
    // for (RegisterRequest request : classDto.getStudent()) {
    // EAccountCredentials student =
    // accountRepository.findByStudentCode(request.studentCode())
    // .orElseThrow(() -> new IllegalArgumentException("Student with code " +
    // request.studentCode() + " not found"));
    //
    // if (student.getRole() != AccountRole.STUDENT) {
    // throw new IllegalArgumentException("Account with code " +
    // request.studentCode() + " is not a student");
    // }
    // student.setStudentClass(existingClass);
    // }
    // }

  }

  @Override
  public void deleteClass(TAccountRequest accountRequest, Long id) {
    classRepository.deleteById(id);
  }

  private void validateDeleteRequest(TAccountRequest accountRequest, Long classId) {
    EClass existingClass = classRepository.findById(classId)
        .orElseThrow(() -> new IllegalArgumentException("Class with ID " + classId + " not found"));

    if (existingClass == null)
      throw new IllegalArgumentException("Class with ID " + classId + " not found");

    if (existingClass.getStatus() == ClassStatus.INACTIVE)
      throw new BadRequestException("Class is Inactive");

    if (accountRequest.role() != AccountRole.ADMIN)
      throw new BadRequestException("No authorization to delete this class");
  }

  @Override
  public ClassVm getClassById(Long classId) {
    EClass eClass = classRepository.findById(classId)
        .orElseThrow(() -> new IllegalArgumentException("Class with ID " + classId + " not found"));

    return ClassVm.builder()
        .className(eClass.getClassName())
        .academicYear(eClass.getAcademicYear())
        .startDate(eClass.getStartDate())
        .endDate(eClass.getEndDate())
        .department(eClass.getDepartment())
        .capacity(eClass.getCapacity())
        .status(eClass.getStatus())
        .build();
  }

  @Override
  public PagedResponse<ClassVm> getAllClasses(Pageable pageable) {
    Page<EClass> classes = classRepository.findAll(pageable);

    List<ClassVm> classVms = classes.getContent().stream()
        .map(EClass -> ClassVm.builder()
            .className(EClass.getClassName())
            .academicYear(EClass.getAcademicYear())
            .startDate(EClass.getStartDate())
            .endDate(EClass.getEndDate())
            .department(EClass.getDepartment())
            .capacity(EClass.getCapacity())
            .status(EClass.getStatus())
            .build())
        .collect(Collectors.toList());

    return PagedResponse.<ClassVm>builder()
        .maxPageItems(pageable.getPageSize())
        .page(pageable.getPageNumber())
        .size(classVms.size())
        .results(classVms)
        .totalElements((int) classes.getTotalElements())
        .totalPages(classes.getTotalPages())
        .build();
  }

  // @Override
  // public PagedResponse<List<ClassVm>> getClassesByDepartment(String department)
  // {
  //
  // List<EClass> classes = classRepository.findByDepartment(department);
  // List<ClassVm> classVms = classes.stream().map(EClass -> ClassVm.builder()
  // .className(EClass.getClassName())
  // .academicYear(EClass.getAcademicYear())
  // .startDate(EClass.getStartDate())
  // .endDate(EClass.getEndDate())
  // .department(EClass.getDepartment())
  // .capacity(EClass.getCapacity())
  // .status(EClass.getStatus())
  // .build()).toList();
  //
  // return PagedResponse.<List<ClassVm>>builder()
  // .maxPageItems(10)
  // .page(1)
  // .results(classVms)
  // .totalElements(classVms.size())
  // .totalPages(1)
  // .build();
  // }

  private void validateClassDto(ClassDto classDto) {
    // Validate required fields
    if ((classDto == null))
      throw new BadRequestException("Class data can not be null");

    if (classDto.getClassName() == null || classDto.getClassName().trim().isEmpty())
      throw new BadRequestException("Class name can not be null");

    if (classDto.getStartDate() == null)
      throw new BadRequestException("Start date is required");

    if (classDto.getEndDate() == null)
      throw new BadRequestException("End date is required");

    if (classDto.getStartDate().isAfter(classDto.getEndDate()))
      throw new BadRequestException("Start date must be before end date");

    if (classDto.getDepartment() == null || classDto.getDepartment().trim().isEmpty())
      throw new BadRequestException("Department is required");

    if (classDto.getCapacity() == null || classDto.getCapacity() < 1)
      throw new BadRequestException("Capacity is required");
  }

  private void validateAccountRequest(TAccountRequest accountRequest) {
    if (accountRequest == null) {
      throw new BadRequestException("Account request cannot be null");
    }
    if (accountRequest.id() == null) {
      throw new BadRequestException("Account ID is required");
    }
    if (accountRequest.username() == null || accountRequest.username().trim().isEmpty()) {
      throw new BadRequestException("Username is required");
    }
    if (accountRequest.role() == null) {
      throw new BadRequestException("Account role is required");
    }
  }
}

package com.winnguyen1905.Activity.model.dto;

import java.time.LocalDate;

import com.winnguyen1905.Activity.common.constant.ClassStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassDto {
    private String className;
    private Integer academicYear;
    private LocalDate startDate;
    private LocalDate endDate;
    private String department;
    private Integer capacity;
    private ClassStatus status;
} 

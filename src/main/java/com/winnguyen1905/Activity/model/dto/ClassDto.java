package com.winnguyen1905.Activity.model.dto;

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
    private String classCode;
    private Integer year;
    private Integer semester;
    private Long facultyId;
} 

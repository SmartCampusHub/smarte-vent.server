package com.winnguyen1905.activity.model.dto;

import com.winnguyen1905.activity.common.constant.ClassStatus;
import com.winnguyen1905.activity.persistance.entity.EAccountCredentials;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.time.LocalDate;
import java.time.Instant;
import java.util.List;

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
  private String createdBy;
  private Instant createdDate;
  private List<RegisterRequest> student;

} 

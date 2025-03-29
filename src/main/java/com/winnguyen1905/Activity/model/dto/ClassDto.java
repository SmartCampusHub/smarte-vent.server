package com.winnguyen1905.Activity.model.dto;

import com.winnguyen1905.Activity.common.constant.ClassStatus;
import com.winnguyen1905.Activity.persistance.entity.EAccountCredentials;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
  private LocalDateTime createdDate;
  private List<RegisterRequest> student;

} 

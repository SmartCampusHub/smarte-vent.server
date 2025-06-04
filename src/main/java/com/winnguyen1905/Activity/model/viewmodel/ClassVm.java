package com.winnguyen1905.Activity.model.viewmodel;

import com.winnguyen1905.Activity.common.constant.ClassStatus;
import com.winnguyen1905.Activity.model.dto.AbstractModel;
import com.winnguyen1905.Activity.persistance.entity.EAccountCredentials;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassVm implements AbstractModel {
  private String className;
  private Integer academicYear;
  private LocalDate startDate;
  private LocalDate endDate;
  private String department;
  private Integer capacity;
  private ClassStatus status;
  private String createdBy;
  private Instant createdDate;
  private List<StudentVm> students;
  public static record StudentVm(String studentName, String identifyCode, String studentEmail) {}
}

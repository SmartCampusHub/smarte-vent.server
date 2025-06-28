package com.winnguyen1905.activity.model.viewmodel;

import com.winnguyen1905.activity.common.constant.ClassStatus;
import com.winnguyen1905.activity.model.dto.AbstractModel;
import com.winnguyen1905.activity.persistance.entity.EAccountCredentials;
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

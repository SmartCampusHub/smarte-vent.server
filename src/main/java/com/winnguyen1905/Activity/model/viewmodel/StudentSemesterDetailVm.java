package com.winnguyen1905.Activity.model.viewmodel;

import com.winnguyen1905.Activity.model.dto.AbstractModel;

import lombok.Builder;

public record StudentSemesterDetailVm(
    Long id,
    Long studentId,
    String classId,
    Integer semesterNumber,
    String semesterYear,
    Integer attendanceScore,
    Integer attendanceScoreFromActivity,
    Float gpa) implements AbstractModel {
  @Builder
  public StudentSemesterDetailVm(
      Long id,
      Long studentId,
      String classId,
      Integer semesterNumber,
      String semesterYear,
      Integer attendanceScore,
      Integer attendanceScoreFromActivity,
      Float gpa) {
    this.id = id;
    this.studentId = studentId;
    this.classId = classId;
    this.semesterNumber = semesterNumber;
    this.semesterYear = semesterYear;
    this.attendanceScore = attendanceScore;
    this.attendanceScoreFromActivity = attendanceScoreFromActivity;
    this.gpa = gpa;
  }
}

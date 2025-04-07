package com.winnguyen1905.Activity.model.viewmodel;

import com.winnguyen1905.Activity.model.dto.AbstractModel;

import lombok.Builder;

@Builder
public record StudentSemesterDetailVm(
    Long id,
    Long studentId,
    String classId,
    Integer attendanceScore,
    Integer attendanceScoreFromActivity,
    Float gpa) implements AbstractModel {
}

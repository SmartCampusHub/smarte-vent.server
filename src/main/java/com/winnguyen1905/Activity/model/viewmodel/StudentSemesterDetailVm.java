package com.winnguyen1905.Activity.model.viewmodel;

import lombok.Builder;

@Builder
public record StudentSemesterDetailVm(
    Long id,
    Long studentId,
    String classId,
    Integer attendanceScore,
    Float gpa) {
}

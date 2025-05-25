package com.winnguyen1905.Activity.model.dto;

public record StudentSemesterDetailDto(
    Long id,
    Long studentId,
    String classId,
    Integer attendanceScore, Float gpa)  implements AbstractModel {
}

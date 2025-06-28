package com.winnguyen1905.activity.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentSemesterDetailDto implements AbstractModel {
    private Long id;
    private Long studentId;
    private String classId;
    private Integer attendanceScore;
    private Float gpa;
}

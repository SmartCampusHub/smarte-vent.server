package com.winnguyen1905.activity.model.viewmodel;

import com.winnguyen1905.activity.model.dto.AbstractModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentSemesterDetailVm implements AbstractModel {
    private Long id;
    private Long studentId;
    private String classId;
    private Integer semesterNumber;
    private String semesterYear;
    private Integer attendanceScore;
    private Integer attendanceScoreFromActivity;
    private Float gpa;
}

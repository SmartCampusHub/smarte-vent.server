package com.winnguyen1905.activity.rest.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.winnguyen1905.activity.common.annotation.AccountRequest;
import com.winnguyen1905.activity.common.annotation.TAccountRequest;
import com.winnguyen1905.activity.common.constant.SystemConstant;
import com.winnguyen1905.activity.model.dto.StatisticsFilterDto;
import com.winnguyen1905.activity.model.viewmodel.StudentStatisticsVm;
import com.winnguyen1905.activity.rest.service.StudentStatisticsService;

import lombok.RequiredArgsConstructor;
@RestController
@RequiredArgsConstructor
@RequestMapping("api/student-statistics")
public class StudentStatisticsController {

    private final StudentStatisticsService studentStatisticsService;
    
    /**
     * Get statistics for the current student
     * This endpoint allows students to view their own participation statistics
     */
    @GetMapping("/my-statistics")
    public ResponseEntity<StudentStatisticsVm> getMyStatistics(@AccountRequest TAccountRequest accountRequest, @ModelAttribute(SystemConstant.MODEL) StatisticsFilterDto filterDto) {
        StudentStatisticsVm statistics = studentStatisticsService.getMyStatistics(accountRequest);
        return ResponseEntity.ok(statistics);
    }
    
    /**
     * Get statistics for a specific student by ID
     * This endpoint is restricted to administrators and organization managers
     */
    @GetMapping("/student/{studentId}")
    public ResponseEntity<StudentStatisticsVm> getStudentStatistics(
            @AccountRequest TAccountRequest accountRequest, @ModelAttribute(SystemConstant.MODEL) StatisticsFilterDto filterDto,
            @PathVariable Long studentId) {
        StudentStatisticsVm statistics = studentStatisticsService.getStudentStatistics(accountRequest, studentId);
        return ResponseEntity.ok(statistics);
    }
}

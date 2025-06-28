package com.winnguyen1905.activity.rest.service;

import com.winnguyen1905.activity.common.annotation.TAccountRequest;
import com.winnguyen1905.activity.model.viewmodel.StudentStatisticsVm;

public interface StudentStatisticsService {
    
    /**
     * Gets the participation statistics for the current student
     * 
     * @param accountRequest The account making the request
     * @return Student statistics view model
     */
    StudentStatisticsVm getMyStatistics(TAccountRequest accountRequest);
    
    /**
     * Gets the participation statistics for a specific student
     * Only accessible by administrators or organization managers
     * 
     * @param accountRequest The account making the request
     * @param studentId The ID of the student to get statistics for
     * @return Student statistics view model
     */
    StudentStatisticsVm getStudentStatistics(TAccountRequest accountRequest, Long studentId);
}

package com.winnguyen1905.Activity.rest.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.winnguyen1905.Activity.common.annotation.AccountRequest;
import com.winnguyen1905.Activity.common.annotation.TAccountRequest;
import com.winnguyen1905.Activity.common.constant.TimePeriod;
import com.winnguyen1905.Activity.model.dto.StatisticsFilterDto;
import com.winnguyen1905.Activity.model.viewmodel.StatisticsVm;
import com.winnguyen1905.Activity.rest.service.StatisticsService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("statistics")
public class StatisticsController {

    private final StatisticsService statisticsService;

    /**
     * Get overall statistics without filtering
     */
    @GetMapping
    public ResponseEntity<StatisticsVm> getStatistics(@AccountRequest TAccountRequest accountRequest) {
        StatisticsVm statistics = statisticsService.getActivityStatistics(accountRequest);
        return ResponseEntity.ok(statistics);
    }

    /**
     * Get filtered statistics with custom filters
     */
    @GetMapping("/filter")
    public ResponseEntity<StatisticsVm> getFilteredStatistics(
            @AccountRequest TAccountRequest accountRequest,
            @ModelAttribute StatisticsFilterDto filterDto) {
        StatisticsVm statistics = statisticsService.getFilteredActivityStatistics(accountRequest, filterDto);
        return ResponseEntity.ok(statistics);
    }

    /**
     * Get statistics for activities in the last day
     */
    @GetMapping("/daily")
    public ResponseEntity<StatisticsVm> getDailyStatistics(@AccountRequest TAccountRequest accountRequest) {
        StatisticsFilterDto filterDto = StatisticsFilterDto.builder()
                .timePeriod(TimePeriod.DAY)
                .build();
        StatisticsVm statistics = statisticsService.getFilteredActivityStatistics(accountRequest, filterDto);
        return ResponseEntity.ok(statistics);
    }

    /**
     * Get statistics for activities in the last week
     */
    @GetMapping("/weekly")
    public ResponseEntity<StatisticsVm> getWeeklyStatistics(@AccountRequest TAccountRequest accountRequest) {
        StatisticsFilterDto filterDto = StatisticsFilterDto.builder()
                .timePeriod(TimePeriod.WEEK)
                .build();
        StatisticsVm statistics = statisticsService.getFilteredActivityStatistics(accountRequest, filterDto);
        return ResponseEntity.ok(statistics);
    }

    /**
     * Get statistics for activities in the last month
     */
    @GetMapping("/monthly")
    public ResponseEntity<StatisticsVm> getMonthlyStatistics(@AccountRequest TAccountRequest accountRequest) {
        StatisticsFilterDto filterDto = StatisticsFilterDto.builder()
                .timePeriod(TimePeriod.MONTH)
                .build();
        StatisticsVm statistics = statisticsService.getFilteredActivityStatistics(accountRequest, filterDto);
        return ResponseEntity.ok(statistics);
    }

    /**
     * Get statistics for activities in the last quarter
     */
    @GetMapping("/quarterly")
    public ResponseEntity<StatisticsVm> getQuarterlyStatistics(@AccountRequest TAccountRequest accountRequest) {
        StatisticsFilterDto filterDto = StatisticsFilterDto.builder()
                .timePeriod(TimePeriod.QUARTER)
                .build();
        StatisticsVm statistics = statisticsService.getFilteredActivityStatistics(accountRequest, filterDto);
        return ResponseEntity.ok(statistics);
    }

    /**
     * Get statistics for activities in the last year
     */
    @GetMapping("/yearly")
    public ResponseEntity<StatisticsVm> getYearlyStatistics(@AccountRequest TAccountRequest accountRequest) {
        StatisticsFilterDto filterDto = StatisticsFilterDto.builder()
                .timePeriod(TimePeriod.YEAR)
                .build();
        StatisticsVm statistics = statisticsService.getFilteredActivityStatistics(accountRequest, filterDto);
        return ResponseEntity.ok(statistics);
    }
}

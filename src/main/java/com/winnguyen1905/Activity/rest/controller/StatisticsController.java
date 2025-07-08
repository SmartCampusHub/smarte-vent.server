package com.winnguyen1905.activity.rest.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.winnguyen1905.activity.common.annotation.AccountRequest;
import com.winnguyen1905.activity.common.annotation.TAccountRequest;
import com.winnguyen1905.activity.common.constant.TimePeriod;
import com.winnguyen1905.activity.model.dto.StatisticsFilterDto;
import com.winnguyen1905.activity.model.viewmodel.StatisticsVm;
import com.winnguyen1905.activity.rest.service.StatisticsService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("statistics")
@Tag(name = "Statistics", description = "Operations for retrieving activity statistics data")
public class StatisticsController {

    private final StatisticsService statisticsService;

    /**
     * Get overall statistics without filtering
     */
    @GetMapping
    @Operation(summary = "Get overall statistics", description = "Get overall statistics without filtering")
    @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully", 
                  content = @Content(schema = @Schema(implementation = StatisticsVm.class))),
      @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<StatisticsVm> getStatistics(
        @Parameter(description = "Account request context", hidden = true) @AccountRequest TAccountRequest accountRequest) {
        StatisticsVm statistics = statisticsService.getActivityStatistics(accountRequest);
        return ResponseEntity.ok(statistics);
    }

    /**
     * Get filtered statistics with custom filters
     */
    @GetMapping("/filter")
    @Operation(summary = "Get filtered statistics", description = "Get statistics filtered by custom criteria")
    @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully", 
                  content = @Content(schema = @Schema(implementation = StatisticsVm.class))),
      @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<StatisticsVm> getFilteredStatistics(
            @Parameter(description = "Account request context", hidden = true) @AccountRequest TAccountRequest accountRequest,
            @Parameter(description = "Statistics filter criteria") @ModelAttribute StatisticsFilterDto filterDto) {
        StatisticsVm statistics = statisticsService.getFilteredActivityStatistics(accountRequest, filterDto);
        return ResponseEntity.ok(statistics);
    }

    /**
     * Get statistics for activities in the last day
     */
    @GetMapping("/daily")
    @Operation(summary = "Get daily statistics", description = "Get statistics for activities in the last day")
    @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Daily statistics retrieved successfully", 
                  content = @Content(schema = @Schema(implementation = StatisticsVm.class))),
      @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<StatisticsVm> getDailyStatistics(
        @Parameter(description = "Account request context", hidden = true) @AccountRequest TAccountRequest accountRequest) {
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
    @Operation(summary = "Get weekly statistics", description = "Get statistics for activities in the last week")
    @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Weekly statistics retrieved successfully", 
                  content = @Content(schema = @Schema(implementation = StatisticsVm.class))),
      @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<StatisticsVm> getWeeklyStatistics(
        @Parameter(description = "Account request context", hidden = true) @AccountRequest TAccountRequest accountRequest) {
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
    @Operation(summary = "Get monthly statistics", description = "Get statistics for activities in the last month")
    @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Monthly statistics retrieved successfully", 
                  content = @Content(schema = @Schema(implementation = StatisticsVm.class))),
      @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<StatisticsVm> getMonthlyStatistics(
        @Parameter(description = "Account request context", hidden = true) @AccountRequest TAccountRequest accountRequest) {
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
    @Operation(summary = "Get quarterly statistics", description = "Get statistics for activities in the last quarter")
    @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Quarterly statistics retrieved successfully", 
                  content = @Content(schema = @Schema(implementation = StatisticsVm.class))),
      @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<StatisticsVm> getQuarterlyStatistics(
        @Parameter(description = "Account request context", hidden = true) @AccountRequest TAccountRequest accountRequest) {
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
    @Operation(summary = "Get yearly statistics", description = "Get statistics for activities in the last year")
    @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Yearly statistics retrieved successfully", 
                  content = @Content(schema = @Schema(implementation = StatisticsVm.class))),
      @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<StatisticsVm> getYearlyStatistics(
        @Parameter(description = "Account request context", hidden = true) @AccountRequest TAccountRequest accountRequest) {
        StatisticsFilterDto filterDto = StatisticsFilterDto.builder()
                .timePeriod(TimePeriod.YEAR)
                .build();
        StatisticsVm statistics = statisticsService.getFilteredActivityStatistics(accountRequest, filterDto);
        return ResponseEntity.ok(statistics);
    }
}

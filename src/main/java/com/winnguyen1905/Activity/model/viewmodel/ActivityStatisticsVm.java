package com.winnguyen1905.activity.model.viewmodel;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import com.winnguyen1905.activity.common.constant.ParticipationRole;
import com.winnguyen1905.activity.common.constant.ParticipationStatus;
import com.winnguyen1905.activity.model.dto.AbstractModel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityStatisticsVm implements AbstractModel {
    private Long activityId;
    private String activityName;
    private String activityCategory;
    private String activityStatus;

    // Participation statistics
    private Integer totalRegistrations;
    private Integer confirmedParticipants;
    private Integer actualAttendees;
    private Double participationRate; // (actual/registered) * 100
    private Double capacityUtilization; // (actual/capacity) * 100

    // Feedback statistics
    private Double averageRating;
    private Integer feedbackCount;
    private Integer highRatingCount; // Rating >= 8
    private Integer midRatingCount; // Rating 4-7
    private Integer lowRatingCount; // Rating < 4

    // Participant role breakdown
    private Map<ParticipationRole, Integer> participantsByRole;

    // Participation status breakdown
    private Map<ParticipationStatus, Integer> participantsByStatus;

    // Timeline statistics
    private Instant createdDate;
    private Instant startDate;
    private Instant endDate;
    private Long durationInHours;
    private Long daysBeforeStart; // How far in advance was it created

    // Top participants (for activities with scores or assessments)
    private List<ParticipantScoreVm> topParticipants;
}

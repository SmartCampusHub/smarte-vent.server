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
public class RepresentativeOrganizerVm implements AbstractModel {
    private Long id;
    private String organizationName;
    private String representativeName;
    private String representativeEmail;
    private String representativePhone;
    private String representativePosition;
}

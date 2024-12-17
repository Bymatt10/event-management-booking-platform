package com.example.backendeventmanagementbooking.domain.dto.request;

import lombok.Data;

import java.util.Date;


@Data
public class UpdateDateDto {
    private Date startDate;

    private Date endDate;
}

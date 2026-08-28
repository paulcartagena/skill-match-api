package com.paulcartagena.skillmatchapi.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PendingRecruiterResponse {

    private Long userId;
    private String email;
}

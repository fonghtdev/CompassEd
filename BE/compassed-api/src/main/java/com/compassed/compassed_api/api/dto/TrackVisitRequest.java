package com.compassed.compassed_api.api.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TrackVisitRequest {
    private String visitorId;
    private String pagePath;
}

package com.smiley.models;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

public class RateRequest {
    @Getter @Setter
    private double rate;
    @Getter @Setter
    private Instant dateRetrieved;
}

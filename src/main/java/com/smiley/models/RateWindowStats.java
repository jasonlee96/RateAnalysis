package com.smiley.models;

import lombok.Data;

@Data
public class RateWindowStats {
    private double avg;
    private double stddev;
    private double p90;
    private double max;
    private double min;
    private int count;
}

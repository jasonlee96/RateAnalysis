package com.smiley.models;

import lombok.Data;

@Data
public class WatcherConfig {
    private boolean enabled;
    private int hourlyWindow = 24;
    private int dailyWindow = 30;
    private double highThreshold = 1.02;
    private double lowThreshold = 0.98;
}

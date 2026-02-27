package com.smiley.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "rate_signals", schema = "ralysis")
public class RateSignalEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter @Setter
    private Long id;

    @Getter @Setter
    private String symbol;

    @Getter @Setter
    private Instant signalDate;

    @Getter @Setter
    private double currentRate;

    @Getter @Setter
    private double hourlyAvg;

    @Getter @Setter
    private double dailyAvg;

    @Getter @Setter
    private double dailyP90;

    @Getter @Setter
    private String signalType;

    @Getter @Setter
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }
}

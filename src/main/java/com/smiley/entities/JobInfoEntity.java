package com.smiley.entities;

import com.smiley.models.enums.JobTypeEnum;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "jobinfos", schema = "ralysis")
public class JobInfoEntity {
    @Id
    @Getter @Setter
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Getter @Setter
    private int jobTypeID;
    @Getter @Setter
    private LocalDate jobValue;
    @Getter @Setter
    private int jobStatusID;
    @Getter @Setter
    private Instant nextJobAt;
    @Getter @Setter
    private Instant createdAt;
    @Getter @Setter
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }
    @PreUpdate
    protected void onUpdate(){
        this.updatedAt = Instant.now();
    }
}

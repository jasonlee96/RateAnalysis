package com.smiley.models.enums;

public enum JobTypeEnum {
    ELT_LOAD(1), ELT_TRANSFORM(2);

    private final int ID;

    JobTypeEnum(int id) { this.ID = id; }

    public int getJobTypeID() { return ID; }
}

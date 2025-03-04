package com.smiley.models.enums;

public enum StatusEnum {
    Pending(0), Success(1), Failed(2);

    private final int ID;

    StatusEnum(int id) { this.ID = id; }

    public int getStatusID() { return ID; }
    // Static method to map an int to an enum
    public static StatusEnum getStatusEnum(int id) {
        for (StatusEnum status : StatusEnum.values()) {
            if (status.getStatusID() == id) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid Status ID: " + id);
    }
}

package kr.co.megabridge.megavnc.enums;

import lombok.Getter;

@Getter
public enum Status {
    OFFLINE("OFFLINE"),
    STANDBY("STANDBY"),
    ACTIVE("ACTIVE");


    private final String value;

    Status(String value) {
        this.value = value;
    }

    public static String toValue(Status status) {
        return status.value;
    }
}

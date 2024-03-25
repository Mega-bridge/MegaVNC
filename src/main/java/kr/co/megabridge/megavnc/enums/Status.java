package kr.co.megabridge.megavnc.enums;

public enum Status {
    OFFLINE_NON_ASSIGNED("OFFLINE_NON_ASSIGNED"),
    OFFLINE_ASSIGNED("OFFLINE_ASSIGNED"),

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

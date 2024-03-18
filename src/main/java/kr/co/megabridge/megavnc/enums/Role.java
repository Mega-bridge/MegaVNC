package kr.co.megabridge.megavnc.enums;

public enum Role {
    ROLE_ADMIN("ROLE_ADMIN"),
    ROLE_USER("ROLE_USER");



    private final String value;

    Role(String value) {
        this.value = value;
    }

    public static String toValue(Role role) {
        return role.value;
    }
}
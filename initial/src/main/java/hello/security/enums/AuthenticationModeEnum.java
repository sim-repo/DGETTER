package hello.security.enums;

public enum AuthenticationModeEnum {
    JWT("JWT"), BASIC("BASIC"), NONE("NONE");

    private final String value;

    AuthenticationModeEnum(String value) {
        this.value = value;
    }

    public static AuthenticationModeEnum fromValue(String value) {
        if (value != null) {
            for (AuthenticationModeEnum e : values()) {
                if (e.value.equals(value)) {
                    return e;
                }
            }
        }
        return getDefault();
    }

    public String toValue() {
        return value;
    }

    public static AuthenticationModeEnum getDefault() {
        return NONE;
    }
}

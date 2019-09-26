package hello.security.enums;

public enum JwtStatusEnum {
    NotAuthenticated("NotAuthenticated"),
    Authorized("Authorized"),
    Expired("Expired"),
    NotAuthorized("NotAuthorized"),
    InternalServerError("InternalServerError"),
    RevokeToken("RevokeToken"),
    NoSettings("NoSettings");

    private final String value;

    JwtStatusEnum(String value) {
        this.value = value;
    }

    public static JwtStatusEnum fromValue(String value) {
        if (value != null) {
            for (JwtStatusEnum e : values()) {
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

    public static JwtStatusEnum getDefault() {
        return NotAuthorized;
    }
}
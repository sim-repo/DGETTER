package hello.enums;

public enum ConnectorEnum {
    mysql("mysql"),
    sqlserver("sqlserver"),
    redis("redis"),
    postgress("postgress");

    private final String value;

    ConnectorEnum(String value) {
        this.value = value;
    }

    public static ConnectorEnum fromValue(String value) {
        if (value != null) {
            for (ConnectorEnum ct : values()) {
                if (ct.value.equals(value)) {
                    return ct;
                }
            }
        }
        return getDefault();
    }

    public String toValue() {
        return value;
    }

    public static ConnectorEnum getDefault() {
        return mysql;
    }
}

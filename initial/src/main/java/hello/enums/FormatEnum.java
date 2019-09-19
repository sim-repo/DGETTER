package hello.enums;

public enum FormatEnum {

    flatXML("flatXML"),
    flatJSON("flatJSON"),
    complexJSON("complexJSON"),
    firstFlatJSON("firstFlatJSON");

    private final String value;

    FormatEnum(String value) {
        this.value = value;
    }

    public static FormatEnum fromValue(String value) {
        if (value != null) {
            for (FormatEnum ct : values()) {
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

    public static FormatEnum getDefault() {
        return flatXML;
    }
}

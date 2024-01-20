package com.petproject.model.enums;

public enum ConvertType {
    MSWORD_DOC("application/msword", ".doc"),
    MSWORD_DOCX("application/vnd.openxmlformats-officedocument.wordprocessingml.document", ".docx"),
    PDF("application/pdf", ".pdf");
    private final String value;

    private final String typeName;

    ConvertType(String value, String typeName) {
        this.value = value;
        this.typeName = typeName;
    }

    public String getTypeName() {
        return typeName;
    }

    public static ConvertType fromValue(String v) {
        for (ConvertType type : ConvertType.values()) {
            if (type.value.equals(v)) return type;
        }
        return null;
    }
}

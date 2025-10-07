package com.project.paypass_renewal.domain;

public enum ServiceCode {
    NONE(0, "no service"),
    PAYPASS_SERVICE(1, "paypass service"),
    CARE_SERVICE(2, "protect service"),
    ALL_SERVICES(3, "all service");

    private final int code;
    private final String description;

    ServiceCode(int code, String description) {
        this.code = code;
        this.description = description;
    }

}

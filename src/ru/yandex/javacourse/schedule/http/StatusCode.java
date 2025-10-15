package ru.yandex.javacourse.schedule.http;

public enum StatusCode {
    OK(200),
    CREATED(201),
    NOT_FOUND(404),
    BAD_REQUEST(400),
    NOT_ACCEPTABLE(406),
    METHOD_NOT_ALLOWED(405),
    INTERNAL_SERVER_ERROR(500);

    private final int code;

    StatusCode(int code) {
        this.code = code;
    }

    public int getCode() {
        return this.code;
    }
}

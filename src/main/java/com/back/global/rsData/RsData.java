package com.back.global.rsData;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * 모든 API 의 공통 응답 형식.
 * resultCode 예) "200-1", "201-1", "404-1"
 * 앞자리 숫자(200, 201, 404 ...)가 실제 HTTP 상태코드로 쓰인다.
 */
public record RsData<T>(
        String resultCode,
        @JsonIgnore int statusCode,
        String msg,
        T data
) {
    public RsData(String resultCode, String msg) {
        this(resultCode, msg, null);
    }

    public RsData(String resultCode, String msg, T data) {
        this(resultCode, Integer.parseInt(resultCode.split("-", 2)[0]), msg, data);
    }
}

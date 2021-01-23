package com.github.mufanh.jsonrpc4j;

import lombok.Getter;
import okhttp3.ResponseBody;

import static com.github.mufanh.jsonrpc4j.Utils.checkNotNull;

/**
 * @author xinquan.huangxq
 */
public final class Response<T> {

    @Getter
    private final okhttp3.Response rawResponse;

    @Getter
    private final T result;

    private Response(okhttp3.Response rawResponse, T result, ResponseBody errorBody) {
        this.rawResponse = rawResponse;
        this.result = result;
    }

    public static <T> Response<T> error(ResponseBody body, okhttp3.Response rawResponse) {
        checkNotNull(body, "body == null");
        checkNotNull(rawResponse, "rawResponse == null");

        if (rawResponse.isSuccessful()) {
            throw new IllegalArgumentException("rawResponse should not be successful response");
        }
        return new Response<>(rawResponse, null, body);
    }

    public static <T> Response<T> success(T result, okhttp3.Response rawResponse) {
        checkNotNull(rawResponse, "rawResponse == null");
        if (!rawResponse.isSuccessful()) {
            throw new IllegalArgumentException("rawResponse must be successful response");
        }
        return new Response<>(rawResponse, result, null);
    }
}

package com.github.mufanh.jsonrpc4j;

import okhttp3.*;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.*;

/**
 * @author xinquan.huangxq
 */
final class RequestBuilder {

    private static final String CONTENT_TYPE = "application/json-rpc";
    private static final String REQUEST_METHOD = "POST";
    private static final String VERSION = "2.0";

    private static final SecureRandom RANDOM = new SecureRandom();

    private final JsonRpcRetrofit jsonRpcRetrofit;

    private final HttpUrl httpUrl;

    private final String methodType;

    private final JsonRpcParamsMode paramsMode;

    private final Object[] args;

    private final Request.Builder requestBuilder;

    private MediaType contentType;

    RequestBuilder(JsonRpcRetrofit jsonRpcRetrofit, HttpUrl httpUrl, Headers headers, String methodType, JsonRpcParamsMode paramsMode, Object[] args) {
        this.jsonRpcRetrofit = jsonRpcRetrofit;

        this.httpUrl = httpUrl;
        this.methodType = methodType;
        this.paramsMode = paramsMode;
        this.args = args;

        this.requestBuilder = new Request.Builder();
        if (headers != null) {
            this.requestBuilder.headers(headers);
        }
    }

    void addHeader(String name, String value) {
        if ("Content-Type".equalsIgnoreCase(name)) {
            MediaType type = MediaType.parse(value);
            if (type == null) {
                throw new IllegalArgumentException("Malformed content type: " + value);
            }
            contentType = type;
        } else {
            requestBuilder.addHeader(name, value);
        }
    }

    Request build() {
        if (contentType == null) {
            contentType = MediaType.parse(CONTENT_TYPE);
        }

        JsonRpcRequest jsonRpcRequest = JsonRpcRequest.builder()
                .id(Math.abs(RANDOM.nextLong()))
                .jsonrpc(VERSION)
                .method(methodType)
                .params(createRequestParams())
                .build();

        String requestBody = jsonRpcRetrofit.jsonBodyConverter.convertRequest(jsonRpcRequest);

        return requestBuilder
                .url(httpUrl)
                .method(REQUEST_METHOD, RequestBody.create(contentType, requestBody))
                .build();
    }

    private Object createRequestParams() {
        if (args == null || args.length == 0) {
            return null;
        }

        if (args.length == 1 && args[0] == null) {
            return null;
        }

        if (paramsMode == JsonRpcParamsMode.OBJECT) {
            if (args.length > 1) {
                throw new IllegalArgumentException("Method cannot has more than 1 arg.");
            } else {
                // ==1
                if (args[0] instanceof Collection) {
                    throw new IllegalArgumentException("The first args can not be collection.");
                }
            }
        }

        if (args.length > 1) {
            return Arrays.asList(args);
        } else {
            // ==1
            if (paramsMode == JsonRpcParamsMode.ARRAY) {
                return Collections.singletonList(args[0]);
            } else {
                if (args[0] instanceof Collection) {
                    Collection<?> collection = (Collection<?>) args[0];
                    if (!collection.isEmpty()) {
                        return new ArrayList<Object>(collection);
                    }
                } else {
                    return args[0];
                }
            }
        }
        return null;
    }
}

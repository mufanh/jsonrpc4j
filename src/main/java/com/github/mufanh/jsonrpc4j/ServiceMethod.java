package com.github.mufanh.jsonrpc4j;

import com.github.mufanh.jsonrpc4j.annotation.JsonRpcMethod;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.ResponseBody;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 * @author xinquan.huangxq
 */
class ServiceMethod<R, T> {

    final JsonRpcRetrofit jsonRpcRetrofit;

    final okhttp3.Call.Factory callFactory;

    final CallAdapter<R, T> callAdapter;

    private final HttpUrl httpUrl;

    private final Headers headers;

    private final String methodType;

    private final JsonRpcParamsMode paramsMode;

    ServiceMethod(Builder<R, T> builder) {
        this.jsonRpcRetrofit = builder.jsonRpcRetrofit;
        this.callFactory = builder.jsonRpcRetrofit.callFactory;
        this.callAdapter = builder.callAdapter;

        this.httpUrl = builder.jsonRpcRetrofit.httpUrl;
        this.headers = builder.jsonRpcRetrofit.headers;

        this.methodType = builder.methodType;
        this.paramsMode = builder.paramsPassMode;
    }

    okhttp3.Call toCall(Object... args) throws IOException {
        RequestBuilder requestBuilder = new RequestBuilder(jsonRpcRetrofit, httpUrl, headers, methodType, paramsMode, args);
        return callFactory.newCall(requestBuilder.build());
    }

    R toResponse(ResponseBody body) throws IOException {
        JsonRpcResponse<R> response;
        try {
            response = jsonRpcRetrofit.jsonBodyConverter.convertResponse(body.string());
        } catch (Exception e) {
            if (e instanceof IOException) {
                throw e;
            } else {
                throw new JsonRpcException("JSON-RPC response convert fail.", e);
            }
        }

        if (response.getError() == null) {
            // 处理成功
            if (response.getResult() == null) {
                throw new JsonRpcException("JSON-RPC response format error, result and error cannot all be null.");
            }
            return response.getResult();
        } else {
            // 处理错误
            if (response.getResult() != null) {
                throw new JsonRpcException("JSON-RPC response format error, result and error cannot all exist.");
            }
            throw new JsonRpcException(response.getError().getCode(), response.getError().getMessage(), response.getError().getData());
        }
    }

    static final class Builder<T, R> {
        final JsonRpcRetrofit jsonRpcRetrofit;
        final Method method;
        final Annotation[] methodAnnotations;
        final int parameterCount;

        Type responseType;

        private String methodType;

        private JsonRpcParamsMode paramsPassMode;

        CallAdapter<T, R> callAdapter;

        Builder(JsonRpcRetrofit jsonRpcRetrofit, Method method) {
            this.jsonRpcRetrofit = jsonRpcRetrofit;
            this.method = method;
            this.parameterCount = method.getParameterCount();

            this.methodAnnotations = method.getAnnotations();
        }

        @SuppressWarnings(value = {"unchecked", "rawtypes"})
        public ServiceMethod build() {
            JsonRpcMethod jsonRpcMethod = Utils.findAnnotation(methodAnnotations, JsonRpcMethod.class);
            if (jsonRpcMethod == null) {
                throw methodError("@JsonRpcMethod is required.");
            }

            this.methodType = jsonRpcMethod.value();
            this.paramsPassMode = jsonRpcMethod.paramsPassMode();

            if (this.paramsPassMode == JsonRpcParamsMode.OBJECT
                    && parameterCount > 1) {
                throw methodError("The function parameter is greater than 1.");
            }

            if (Utils.isEmpty(methodType)) {
                throw methodError("MethodType is required.");
            }

            callAdapter = createCallAdapter();

            responseType = callAdapter.responseType();
            if (responseType == Response.class || responseType == okhttp3.Response.class) {
                throw methodError("'"
                        + Utils.getRawType(responseType).getName()
                        + "' is not a valid response body type. Did you mean ResponseBody?");
            }

            return new ServiceMethod(this);
        }

        @SuppressWarnings("unchecked")
        private CallAdapter<T, R> createCallAdapter() {
            Type returnType = method.getGenericReturnType();
            if (Utils.hasUnresolvableType(returnType)) {
                throw methodError("Method return type must not include a type variable or wildcard: %s", returnType);
            }
            if (returnType == void.class) {
                throw methodError("Service methods cannot return void.");
            }
            Annotation[] annotations = method.getAnnotations();
            try {
                return (CallAdapter<T, R>) jsonRpcRetrofit.callAdapter(returnType, annotations);
            } catch (RuntimeException e) {
                throw methodError(e, "Unable to create call adapter for %s", returnType);
            }
        }

        private RuntimeException methodError(String message, Object... args) {
            return methodError(null, message, args);
        }

        private RuntimeException methodError(Throwable cause, String message, Object... args) {
            return new IllegalArgumentException(String.format(message, args)
                    + "\n    for method "
                    + method.getDeclaringClass().getSimpleName()
                    + "."
                    + method.getName(), cause);
        }
    }
}
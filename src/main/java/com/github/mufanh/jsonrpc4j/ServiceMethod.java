package com.github.mufanh.jsonrpc4j;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.mufanh.jsonrpc4j.annotation.JsonRpcMethod;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.ResponseBody;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import static com.github.mufanh.jsonrpc4j.JsonRpcConstants.*;
import static com.github.mufanh.jsonrpc4j.JsonUtils.*;
import static com.github.mufanh.jsonrpc4j.Utils.*;

/**
 * @author xinquan.huangxq
 */
class ServiceMethod<R, T> {

    private final okhttp3.Call.Factory callFactory;

    private final CallAdapter<R, T> callAdapter;

    private final HttpUrl httpUrl;

    private final Headers headers;

    private final String methodType;

    private final JsonRpcParamsPassMode paramsPassMode;

    ServiceMethod(Builder<R, T> builder) {
        this.callFactory = builder.jsonRpcRetrofit.callFactory;
        this.callAdapter = builder.callAdapter;

        this.httpUrl = builder.jsonRpcRetrofit.httpUrl;
        this.headers = builder.jsonRpcRetrofit.headers;

        this.methodType = builder.methodType;
        this.paramsPassMode = builder.paramsPassMode;
    }

    okhttp3.Call toCall(Object... args) throws IOException {
        RequestBuilder requestBuilder = new RequestBuilder(httpUrl, headers, methodType, paramsPassMode, args);
        return callFactory.newCall(requestBuilder.build());
    }

    T adapt(Call<R> call) {
        return callAdapter.adapt(call);
    }

    R toResponse(ResponseBody body) throws IOException {
        JsonNode jsonNode = readTree(body.bytes());
        if (hasError(jsonNode)) {
            Throwable throwable = resolveException(jsonNode);
            if (throwable instanceof IOException) {
                throw (IOException) throwable;
            } else {
                throw new IOException(throwable);
            }
        }
        if (hasResult(jsonNode)) {
            if (isReturnTypeInvalid(callAdapter.responseType())) {
                return null;
            }
            return constructResponseObject(callAdapter.responseType(), jsonNode);
        }
        return null;
    }

    private <T> T constructResponseObject(Type returnType, JsonNode jsonObject) throws IOException {
        JsonParser returnJsonParser = treeAsTokens(jsonObject.get(RESULT));
        JavaType returnJavaType = getTypeFactory().constructType(returnType);
        return readValue(returnJsonParser, returnJavaType);
    }

    private boolean hasResult(JsonNode jsonObject) {
        return hasNonNullData(jsonObject, RESULT);
    }

    private boolean isReturnTypeInvalid(Type returnType) {
        if (returnType == null || returnType == Void.class) {
            return true;
        }
        return false;
    }

    protected boolean hasError(JsonNode jsonObject) {
        return jsonObject.has(ERROR) && jsonObject.get(ERROR) != null && !jsonObject.get(ERROR).isNull();
    }

    public Throwable resolveException(JsonNode jsonNode) {
        JsonNode errorObject = jsonNode.get(ERROR);
        if (!hasNonNullObjectData(errorObject, DATA))
            return createJsonRpcClientException(errorObject);

        JsonNode dataObject = errorObject.get(DATA);
        if (!hasNonNullTextualData(dataObject, EXCEPTION_TYPE_NAME))
            return createJsonRpcClientException(errorObject);

        try {
            String exceptionTypeName = dataObject.get(EXCEPTION_TYPE_NAME).asText();
            String message = hasNonNullTextualData(dataObject, ERROR_MESSAGE) ? dataObject.get(ERROR_MESSAGE).asText() : null;
            return createThrowable(exceptionTypeName, message);
        } catch (Exception e) {
            return createJsonRpcClientException(errorObject);
        }
    }

    private JsonRpcClientException createJsonRpcClientException(JsonNode errorObject) {
        int code = errorObject.has(ERROR_CODE) ? errorObject.get(ERROR_CODE).asInt() : 0;
        return new JsonRpcClientException(code, errorObject.get(ERROR_MESSAGE).asText(), errorObject.get(DATA));
    }

    protected Class<? extends Throwable> resolveThrowableClass(String typeName) throws ClassNotFoundException {
        Class<?> clazz;
        try {
            clazz = Class.forName(typeName);
            if (!Throwable.class.isAssignableFrom(clazz)) {
            } else {
                return clazz.asSubclass(Throwable.class);
            }
        } catch (ClassNotFoundException e) {
            throw e;
        } catch (Exception e) {
        }
        return null;
    }

    private Throwable createThrowable(String typeName, String message) throws IllegalAccessException, InvocationTargetException, InstantiationException, ClassNotFoundException {
        Class<? extends Throwable> clazz = resolveThrowableClass(typeName);

        Constructor<? extends Throwable> defaultCtr = getDefaultConstructor(clazz);
        Constructor<? extends Throwable> messageCtr = getMessageConstructor(clazz);

        if (message != null && messageCtr != null) {
            return messageCtr.newInstance(message);
        } else if (message != null && defaultCtr != null) {
            return defaultCtr.newInstance();
        } else if (message == null && defaultCtr != null) {
            return defaultCtr.newInstance();
        } else if (message == null && messageCtr != null) {
            return messageCtr.newInstance((String) null);
        } else {
            return null;
        }
    }

    private Constructor<? extends Throwable> getMessageConstructor(Class<? extends Throwable> clazz) {
        Constructor<? extends Throwable> messageCtr = null;
        try {
            messageCtr = clazz.getConstructor(String.class);
        } catch (NoSuchMethodException ignored) {
        }
        return messageCtr;
    }

    private Constructor<? extends Throwable> getDefaultConstructor(Class<? extends Throwable> clazz) {
        Constructor<? extends Throwable> defaultCtr = null;
        try {
            defaultCtr = clazz.getConstructor();
        } catch (NoSuchMethodException ignore) {
        }
        return defaultCtr;
    }

    static final class Builder<T, R> {
        final JsonRpcRetrofit jsonRpcRetrofit;
        final Method method;
        final Annotation[] methodAnnotations;
        final int parameterCount;

        Type responseType;

        private String methodType;

        private JsonRpcParamsPassMode paramsPassMode;

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

            if (this.paramsPassMode == JsonRpcParamsPassMode.OBJECT
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
            message = String.format(message, args);
            return new IllegalArgumentException(message
                    + "\n    for method "
                    + method.getDeclaringClass().getSimpleName()
                    + "."
                    + method.getName(), cause);
        }
    }
}
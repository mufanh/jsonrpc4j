package com.github.mufanh.jsonrpc4j;


import com.github.mufanh.jsonrpc4j.annotation.JsonRpcService;
import okhttp3.*;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

import static com.github.mufanh.jsonrpc4j.Utils.checkNotNull;

/**
 * @author xinquan.huangxq
 */
public class JsonRpcRetrofit {

    private final Map<Method, ServiceMethod<?, ?>> serviceMethodCache = new ConcurrentHashMap<>();

    final okhttp3.Call.Factory callFactory;

    final HttpUrl httpUrl;

    final Headers headers;

    final boolean validateEagerly;

    final List<CallAdapter.Factory> callAdapterFactories;

    final Executor callbackExecutor;

    final JsonBodyConverter jsonBodyConverter;

    private JsonRpcRetrofit(okhttp3.Call.Factory callFactory, HttpUrl httpUrl, Headers headers,
                            List<CallAdapter.Factory> callAdapterFactories,
                            Executor callbackExecutor, JsonBodyConverter jsonBodyConverter, boolean validateEagerly) {
        this.callFactory = callFactory;
        this.httpUrl = httpUrl;
        this.headers = headers;
        this.callAdapterFactories = callAdapterFactories;
        this.callbackExecutor = callbackExecutor;
        this.jsonBodyConverter = jsonBodyConverter;
        this.validateEagerly = validateEagerly;
    }

    @SuppressWarnings("unchecked")
    public <T> T create(final Class<T> service) {
        validateServiceInterface(service);
        if (validateEagerly) {
            eagerlyValidateMethods(service);
        }

        return (T) Proxy.newProxyInstance(service.getClassLoader(), new Class<?>[]{service},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        if (method.getDeclaringClass() == Object.class) {
                            return method.invoke(this, args);
                        }
                        if (method.isDefault()) {
                            return invokeDefaultMethod(method, service, proxy, args);
                        }
                        ServiceMethod<Object, Object> serviceMethod =
                                (ServiceMethod<Object, Object>) loadServiceMethod(method);
                        OkHttpCall<Object> okHttpCall = new OkHttpCall<>(serviceMethod, args);
                        return serviceMethod.callAdapter.adapt(okHttpCall);
                    }
                });
    }

    public CallAdapter<?, ?> callAdapter(Type returnType, Annotation[] annotations) {
        return nextCallAdapter(null, returnType, annotations);
    }

    public CallAdapter<?, ?> nextCallAdapter(CallAdapter.Factory skipPast, Type returnType,
                                             Annotation[] annotations) {
        checkNotNull(returnType, "returnType == null");
        checkNotNull(annotations, "annotations == null");

        int start = callAdapterFactories.indexOf(skipPast) + 1;
        for (int i = start, count = callAdapterFactories.size(); i < count; i++) {
            CallAdapter<?, ?> adapter = callAdapterFactories.get(i).get(returnType, annotations, this);
            if (adapter != null) {
                return adapter;
            }
        }

        StringBuilder builder = new StringBuilder("Could not locate call adapter for ")
                .append(returnType)
                .append(".\n");
        if (skipPast != null) {
            builder.append("  Skipped:");
            for (int i = 0; i < start; i++) {
                builder.append("\n   * ").append(callAdapterFactories.get(i).getClass().getName());
            }
            builder.append('\n');
        }
        builder.append("  Tried:");
        for (int i = start, count = callAdapterFactories.size(); i < count; i++) {
            builder.append("\n   * ").append(callAdapterFactories.get(i).getClass().getName());
        }
        throw new IllegalArgumentException(builder.toString());
    }

    private void eagerlyValidateMethods(Class<?> service) {
        for (Method method : service.getDeclaredMethods()) {
            if (!method.isDefault()) {
                loadServiceMethod(method);
            }
        }
    }

    @SuppressWarnings("rawtypes")
    private ServiceMethod<?, ?> loadServiceMethod(Method method) {
        ServiceMethod<?, ?> result = serviceMethodCache.get(method);
        if (result != null) return result;

        synchronized (serviceMethodCache) {
            result = serviceMethodCache.get(method);
            if (result == null) {
                result = new ServiceMethod.Builder(this, method).build();
                serviceMethodCache.put(method, result);
            }
        }
        return result;
    }

    public static final class Builder {

        private okhttp3.Call.Factory callFactory;

        private HttpUrl httpUrl;

        private Headers headers;

        private boolean validateEagerly;

        private Executor callbackExecutor;

        private JsonBodyConverter jsonBodyConverter;

        private final List<CallAdapter.Factory> callAdapterFactories = new ArrayList<>();

        public Builder callFactory(okhttp3.Call.Factory factory) {
            this.callFactory = checkNotNull(factory, "factory == null");
            return this;
        }

        public Builder headers(Headers headers) {
            this.headers = headers;
            return this;
        }

        public Builder httpUrl(HttpUrl httpUrl) {
            this.httpUrl = httpUrl;
            return this;
        }

        public Builder httpUrl(String httpUrl) {
            checkNotNull(httpUrl, "httpUrl == null");
            HttpUrl url = HttpUrl.parse(httpUrl);
            if (url == null) {
                throw new IllegalArgumentException("Illegal URL: " + httpUrl);
            }
            return httpUrl(url);
        }

        public Builder jsonBodyConverter(JsonBodyConverter jsonBodyConverter) {
            this.jsonBodyConverter = jsonBodyConverter;
            return this;
        }

        public Builder validateEagerly(boolean validateEagerly) {
            this.validateEagerly = validateEagerly;
            return this;
        }

        public Builder callbackExecutor(Executor executor) {
            this.callbackExecutor = checkNotNull(executor, "executor == null");
            return this;
        }

        public Builder addCallAdapterFactory(CallAdapter.Factory factory) {
            callAdapterFactories.add(checkNotNull(factory, "factory == null"));
            return this;
        }

        public JsonRpcRetrofit build() {
            if (httpUrl == null) {
                throw new IllegalStateException("HTTP URL required.");
            }
            if (jsonBodyConverter == null) {
                throw new IllegalStateException("JSON Converter required.");
            }

            okhttp3.Call.Factory callFactory = this.callFactory;
            if (callFactory == null) {
                callFactory = new OkHttpClient();
            }

            Executor callbackExecutor = this.callbackExecutor;

            List<CallAdapter.Factory> callAdapterFactories = new ArrayList<>(this.callAdapterFactories);
            if (callbackExecutor != null) {
                callAdapterFactories.add(new ExecutorCallAdapterFactory(callbackExecutor));
            } else {
                callAdapterFactories.add(DefaultCallAdapterFactory.INSTANCE);
            }

            return new JsonRpcRetrofit(callFactory, httpUrl, headers,
                    callAdapterFactories, callbackExecutor, jsonBodyConverter, validateEagerly);
        }
    }

    private static <T> void validateServiceInterface(Class<T> service) {
        if (service == null) {
            throw new IllegalArgumentException("Interface must not be null.");
        }
        if (!service.isInterface()) {
            throw new IllegalArgumentException("API declarations must be interfaces.");
        }
        if (service.getInterfaces().length > 0) {
            throw new IllegalArgumentException("API interfaces must not extend other interfaces.");
        }
        if (!Utils.isAnnotationPresent(service.getAnnotations(), JsonRpcService.class)) {
            throw new IllegalArgumentException("API interfaces must has @JsonRpcService annotation.");
        }
    }

    /**
     * 执行接口中的默认方法
     *
     * @param method
     * @param declaringClass
     * @param object
     * @param args
     * @return
     * @throws Throwable
     */
    private Object invokeDefaultMethod(Method method, Class<?> declaringClass, Object object, Object... args)
            throws Throwable {
        Constructor<MethodHandles.Lookup> constructor = MethodHandles.Lookup.class.getDeclaredConstructor(Class.class, int.class);
        constructor.setAccessible(true);
        return constructor.newInstance(declaringClass, -1 /* trusted */)
                .unreflectSpecial(method, declaringClass)
                .bindTo(object)
                .invokeWithArguments(args);
    }
}

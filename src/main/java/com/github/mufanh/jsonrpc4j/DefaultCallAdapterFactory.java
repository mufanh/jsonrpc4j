package com.github.mufanh.jsonrpc4j;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * @author xinquan.huangxq
 */
public class DefaultCallAdapterFactory extends CallAdapter.Factory {

    static final CallAdapter.Factory INSTANCE = new DefaultCallAdapterFactory();

    @Override
    public CallAdapter<?, ?> get(Type returnType, Annotation[] annotations, JsonRpcRetrofit retrofit) {
        if (getRawType(returnType) != Call.class) {
            return null;
        }

        final Type responseType = Utils.getCallResponseType(returnType);
        return new CallAdapter<Object, Call<?>>() {
            @Override
            public Type responseType() {
                return responseType;
            }

            @Override
            public Call<Object> adapt(Call<Object> call) {
                return call;
            }
        };
    }
}
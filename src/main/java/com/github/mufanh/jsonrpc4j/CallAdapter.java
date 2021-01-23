package com.github.mufanh.jsonrpc4j;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * @author xinquan.huangxq
 */
public interface CallAdapter<R, T> {

    Type responseType();

    T adapt(Call<R> call);

    abstract class Factory {
        public abstract CallAdapter<?, ?> get(Type returnType, Annotation[] annotations, JsonRpcRetrofit retrofit);

        protected static Type getParameterUpperBound(int index, ParameterizedType type) {
            return Utils.getParameterUpperBound(index, type);
        }

        protected static Class<?> getRawType(Type type) {
            return Utils.getRawType(type);
        }
    }
}

package com.github.mufanh.jsonrpc4j;

import okhttp3.Request;

import java.io.IOException;

/**
 * @author xinquan.huangxq
 */
public interface Call<T> extends Cloneable {

    Response<T> execute() throws IOException;

    void enqueue(Callback<T> callback);

    boolean isExecuted();

    void cancel();

    boolean isCanceled();

    Call<T> clone();

    Request request();
}

package com.github.mufanh.jsonrpc4j;

/**
 * @author xinquan.huangxq
 */
public interface Callback<T> {

    void onResponse(Call<T> call, Response<T> response);

    void onFailure(Call<T> call, Throwable t);
}

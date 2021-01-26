package com.github.mufanh.jsonrpc4j;

import java.lang.reflect.Type;

/**
 * @author xinquan.huangxq
 */
public interface JsonBodyConverter {

    /**
     * 请求报文转换
     *
     * @param request
     * @return
     * @throws JsonConvertException
     */
    String convertRequest(JsonRpcRequest request) throws JsonConvertException;

    /**
     * 响应报文转换
     *
     * @param type
     * @param response
     * @param <T>
     * @return
     * @throws JsonConvertException
     */
    <T> JsonRpcResponse<T> convertResponse(Type type, String response) throws JsonConvertException;
}

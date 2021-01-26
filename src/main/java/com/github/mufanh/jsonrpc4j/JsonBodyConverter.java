package com.github.mufanh.jsonrpc4j;

import java.io.IOException;

/**
 * @author xinquan.huangxq
 */
public interface JsonBodyConverter {

    /**
     * 请求报文转换
     *
     * @param request
     * @param <T>
     * @return
     * @throws IOException
     */
    String convertRequest(JsonRpcRequest request) throws IOException;

    /**
     * 响应报文转换
     *
     * @param response
     * @param <T>
     * @return
     * @throws IOException
     */
    <T> JsonRpcResponse<T> convertResponse(String response) throws IOException;
}

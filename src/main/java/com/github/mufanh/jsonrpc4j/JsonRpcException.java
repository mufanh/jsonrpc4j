package com.github.mufanh.jsonrpc4j;

import lombok.Getter;

/**
 * @author xinquan.huangxq
 */
@Getter
public class JsonRpcException extends RuntimeException {

    private final int code;

    private final Object data;

    public JsonRpcException(String message) {
        super(message);
        this.code = -32603;
        this.data = null;
    }

    public JsonRpcException(String message, Throwable e) {
        super(message, e);
        this.code = -32603;
        this.data = null;
    }

    public JsonRpcException(int code, String message) {
        super(message);
        this.code = code;
        this.data = null;
    }

    public JsonRpcException(int code, String message, Object data) {
        super(message);
        this.code = code;
        this.data = data;
    }
}

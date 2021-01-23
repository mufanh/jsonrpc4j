package com.github.mufanh.jsonrpc4j;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;

/**
 * @author xinquan.huangxq
 */
@Getter
public class JsonRpcClientException extends RuntimeException {

    private final int code;

    private final JsonNode data;

    public JsonRpcClientException(int code, String message, JsonNode data) {
        super(message);
        this.code = code;
        this.data = data;
    }
}

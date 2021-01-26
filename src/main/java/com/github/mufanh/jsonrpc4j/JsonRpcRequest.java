package com.github.mufanh.jsonrpc4j;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * @author xinquan.huangxq
 */
@Builder
@Data
public final class JsonRpcRequest implements Serializable {

    private long id;

    // 2.0
    private String jsonrpc;

    private String method;

    private Object params;
}

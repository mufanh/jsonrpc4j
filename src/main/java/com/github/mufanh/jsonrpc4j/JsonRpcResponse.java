package com.github.mufanh.jsonrpc4j;

import lombok.Data;

import java.io.Serializable;

/**
 * @author xinquan.huangxq
 */
@Data
public final class JsonRpcResponse<T> implements Serializable {

    private Long id;

    // 2.0
    private String jsonrpc;

    /**
     * 该成员在成功时必须包含
     * 当调用方法引起错误时必须不包含该成员
     * 服务端中的被调用方法决定了该成员的值
     */
    private T result;

    /**
     * 该成员在失败是必须包含
     * 当没有引起错误的时必须不包含该成员
     */
    private Error error;

    /**
     * JSON-RPC 2.0规范建议：
     * -32700	        Parse error语法解析错误	    服务端接收到无效的json。该错误发送于服务器尝试解析json文本
     * -32600	        Invalid Request无效请求	    发送的json不是一个有效的请求对象。
     * -32601	        Method not found找不到方法	该方法不存在或无效
     * -32602	        Invalid params无效的参数	    无效的方法参数。
     * -32603	        Internal error内部错误	    JSON-RPC内部错误。
     * -32000 ~ -32099	Server error服务端错误	    预留用于自定义的服务器错误
     */
    @Data
    public static final class Error {

        // 使用数值表示该异常的错误类型
        private int code;

        private String message;

        private String data;
    }
}

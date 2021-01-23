package com.github.mufanh.jsonrpc4j;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import okhttp3.*;

import java.security.SecureRandom;
import java.util.Collection;

import static com.github.mufanh.jsonrpc4j.JsonRpcConstants.*;
import static com.github.mufanh.jsonrpc4j.JsonUtils.*;

/**
 * @author xinquan.huangxq
 */
final class RequestBuilder {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final HttpUrl httpUrl;

    private final String methodType;

    private final JsonRpcParamsPassMode paramsPassMode;

    private final Object[] args;

    private final Request.Builder requestBuilder;

    private MediaType contentType;

    RequestBuilder(HttpUrl httpUrl, Headers headers, String methodType, JsonRpcParamsPassMode paramsPassMode, Object[] args) {
        this.httpUrl = httpUrl;
        this.methodType = methodType;
        this.paramsPassMode = paramsPassMode;
        this.args = args;

        this.requestBuilder = new Request.Builder();
        if (headers != null) {
            this.requestBuilder.headers(headers);
        }
    }

    void addHeader(String name, String value) {
        if ("Content-Type".equalsIgnoreCase(name)) {
            MediaType type = MediaType.parse(value);
            if (type == null) {
                throw new IllegalArgumentException("Malformed content type: " + value);
            }
            contentType = type;
        } else {
            requestBuilder.addHeader(name, value);
        }
    }

    Request build() {
        if (contentType == null) {
            contentType = MediaType.parse(JsonRpcConstants.JSONRPC_CONTENT_TYPE);
        }

        ObjectNode contentNode = createObjectNode()
                .put(ID, RANDOM.nextLong())
                .put(JSONRPC, VERSION)
                .put(METHOD, methodType);
        JsonNode paramsNode = buildParamsNode();
        if (paramsNode != null) {
            contentNode.set(PARAMS, paramsNode);
        }

        return requestBuilder
                .url(httpUrl)
                .method(JsonRpcConstants.JSONRPC_REQUEST_METHOD,
                        RequestBody.create(contentType, contentNode.toString()))
                .build();
    }

    private JsonNode buildParamsNode() {
        if (args == null || args.length == 0) {
            return null;
        }

        if (args.length == 1 && args[0] == null) {
            return null;
        }

        if (paramsPassMode == JsonRpcParamsPassMode.OBJECT) {
            if (args.length > 1) {
                throw new IllegalArgumentException("Method cannot has more than 1 arg.");
            } else {
                // ==1
                if (args[0] instanceof Collection) {
                    throw new IllegalArgumentException("The first args can not be collection.");
                }
            }
        }

        if (args.length > 1) {
            ArrayNode paramsNode = new ArrayNode(getNodeFactory());
            for (Object arg : args) {
                paramsNode.add(valueToTree(arg));
            }
            return paramsNode;
        } else {
            // == 1
            if (args[0] instanceof Collection) {
                Collection<?> collection = (Collection<?>) args[0];
                if (!collection.isEmpty()) {
                    ArrayNode paramsNode = new ArrayNode(getNodeFactory());
                    for (Object arg : collection) {
                        JsonNode argNode = valueToTree(arg);
                        paramsNode.add(argNode);
                    }
                    return paramsNode;
                }
            } else {
                if (paramsPassMode == JsonRpcParamsPassMode.ARRAY) {
                    ArrayNode paramsNode = createArrayNode();
                    paramsNode.add(valueToTree(args[0]));
                    return paramsNode;
                } else {
                    return valueToTree(args[0]);
                }
            }
        }

        return null;
    }
}

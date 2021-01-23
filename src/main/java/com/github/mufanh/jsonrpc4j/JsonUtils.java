package com.github.mufanh.jsonrpc4j;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.type.TypeFactory;

import java.io.IOException;

/**
 * @author xinquan.huangxq
 */
class JsonUtils {

    private static final ObjectMapper mapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private JsonUtils() {
        throw new AssertionError("Cannot be instantiated");
    }

    public static ObjectNode createObjectNode() {
        return mapper.createObjectNode();
    }

    public static JsonNodeFactory getNodeFactory() {
        return mapper.getNodeFactory();
    }

    public static <T extends JsonNode> T valueToTree(Object fromValue) throws IllegalArgumentException {
        return mapper.valueToTree(fromValue);
    }

    public static ArrayNode createArrayNode() {
        return mapper.createArrayNode();
    }

    public static JsonNode readTree(byte[] bytes) throws IOException {
        return mapper.readTree(bytes);
    }

    public static TypeFactory getTypeFactory() {
        return mapper.getTypeFactory();
    }

    public static <T> T readValue(JsonParser jsonParser, JavaType javaType) throws IOException {
        return mapper.readValue(jsonParser, javaType);
    }

    public static JsonParser treeAsTokens(TreeNode treeNode) {
        return mapper.treeAsTokens(treeNode);
    }
}

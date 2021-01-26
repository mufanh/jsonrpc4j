package com.github.mufanh.jsonrpc4j.annotation;

import com.github.mufanh.jsonrpc4j.JsonRpcParamsMode;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author xinquan.huangxq
 */
@Target(METHOD)
@Retention(RUNTIME)
public @interface JsonRpcMethod {

    String value();

    JsonRpcParamsMode paramsPassMode() default JsonRpcParamsMode.AUTO;
}

package com.github.mufanh.jsonrpc4j.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author xinquan.huangxq
 */
@Target(TYPE)
@Retention(RUNTIME)
public @interface JsonRpcService {
}

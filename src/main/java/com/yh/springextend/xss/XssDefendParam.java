package com.yh.springextend.xss;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @ClassName XssDefendParam
 * @Description
 * @Author yh
 * @Date 2020-06-24 16:26
 * @Version 1.0
 */
@Documented
@Target({ FIELD })
@Retention(RUNTIME)
public @interface XssDefendParam {

    boolean value() default true;
}

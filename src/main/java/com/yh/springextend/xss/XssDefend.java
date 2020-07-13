package com.yh.springextend.xss;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @ClassName XssDefend
 * @Description xss防御注解
 * @Author yh
 * @Date 2020-06-24 16:19
 * @Version 1.0
 */
@Documented
@Target({ TYPE })
@Retention(RUNTIME)
public @interface XssDefend {

    XssDefendType type() default XssDefendType.ALL;
}

package com.yh.springextend.swagger.annotation;

import java.lang.annotation.*;

/**
 * @author yjx
 * @date 2019-08-07
 * 自定义构建Operation的插件,解析的注解
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CustomResponseModel {
    /**
     * 返回实体封装类class
     * @return
     */
    Class classResponse();

    /**
     * 返回实体封装类里面data属性的class
     * @return
     */
    Class typeBinding() default Object.class;

    /**
     * 分页时使用,列表里面存储的实体类class
     * @return
     */
    Class queryListTypeBinding() default Object.class;
    /**
     * 分页时使用,列表里面存储的实体类class
     * @return
     */
    Class footListTypeBinding() default Object.class;

    /**
     * 是否有分页,有分页设置为true
     * @return
     */
    boolean isPage() default false;
}

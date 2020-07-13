package com.yh.springextend.xss;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import org.apache.logging.log4j.spi.LoggerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.MediaType;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;

/**
 * @ClassName XssFastJsonHttpMessageConverter
 * @Description xss转换
 * @Author yh
 * @Date 2020-06-24 16:16
 * @Version 1.0
 */
public class XssFastJsonHttpMessageConverter extends FastJsonHttpMessageConverter {

    private static final Logger LOGGER = LoggerFactory.getLogger(XssFastJsonHttpMessageConverter.class);

    private String basePackage;

    public XssFastJsonHttpMessageConverter(String basePackage) {
        this.basePackage = basePackage;
    }

    @Override
    public boolean canRead(Type type, Class<?> clazz, MediaType mediaType) {
        XssDefend xssDefend = AnnotationUtils.findAnnotation((Class) type,XssDefend.class);
        if (xssDefend != null &&"application".equals(mediaType.getType())&&"json".equals(mediaType.getSubtype())) {
            return super.canRead(clazz,mediaType);
        } else {
            return false;
        }
    }

    @Override
    public Object read(Type type, Class<?> contextClass, HttpInputMessage inputMessage) throws IOException {
        Object object = super.read(type,contextClass,inputMessage);
        return resolveMvcModel(object);
    }

    /**
     *  对交付给handler的对象进行xss过滤
     * @param object
     * @return
     */
    private Object resolveMvcModel(Object object) {
        XssDefend xssDefend = AnnotationUtils.findAnnotation(object.getClass(),XssDefend.class);
        if (xssDefend == null) {
            return object;
        }
        if (xssDefend.type() == XssDefendType.ALL) {
            //递归对所有字段进行处理
            return resolveAll(object);
        } else if (xssDefend.type() == XssDefendType.ANNOTATED) {
            //递归对带有注解的字段进行处理
            return resolveAnnotated(object);
        } else {
            return object;
        }
    }

    /**
     * 处理所有字段
     * @param object
     * @return
     */
    private Object resolveAll(Object object) {
        //对所有String类型字段做xss处理
        for (Field field : object.getClass().getDeclaredFields()) {
            try {
                Class type = field.getType();
                if(isCandidateType(type)) {
                    PropertyDescriptor descriptor = new PropertyDescriptor(field.getName(),object.getClass());
                    doXssDefendForProperty(object,descriptor,XssDefendType.ALL);
                }
            } catch (Exception e) {
                LOGGER.error(String.format("[xss消息转换器]消息转换器异常，可能存在未经xss过滤的值:%s",JSON.toJSONString(object)),e);
                return object;
            }
        }
        return object;
    }

    /**
     * 处理@XssDefendParam注解的字段
     * @param object
     * @return
     */
    private Object resolveAnnotated(Object object) {
        //对带有注解的String类型字段做xss处理
        for (Field field : object.getClass().getDeclaredFields()) {
            try {
                if (isCandidateType(field.getType())) {
                    PropertyDescriptor descriptor = new PropertyDescriptor(field.getName(),object.getClass());
                    XssDefendParam xssDefendParam = AnnotationUtils.findAnnotation(field,XssDefendParam.class);
                    if(xssDefendParam == null || !xssDefendParam.value()) {
                        continue;
                    }
                    doXssDefendForProperty(object,descriptor,XssDefendType.ANNOTATED);
                }
            } catch (Exception e) {
                LOGGER.error(String.format("[xss消息转换器]消息转换器异常，可能存在未经xss过滤的值:%s",JSON.toJSONString(object)),e);
                return object;
            }
        }
        return object;
    }

    /**
     * 对目标属性做xss过滤
     * @param object
     * @param descriptor
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    private void doXssDefendForProperty(Object object, PropertyDescriptor descriptor,XssDefendType defendType) throws InvocationTargetException, IllegalAccessException {
        Method reader = descriptor.getReadMethod();
        if ( reader == null) {
            return;
        }
        Object val = reader.invoke(object,null);
        if (val == null) {
            return;
        }
        if (val instanceof String) {
           resolveString(object,(String)val,descriptor);
        } else if (val instanceof Collection){
            Collection collection = (Collection) val;
            resolveCollection(collection);
        } else if (val instanceof Map) {
            Map map = (Map)val;
            resolveMap(map);
        } else if (defendType == XssDefendType.ALL && isBasePackage(object)) {
            resolveAll(val);
        } else if (defendType == XssDefendType.ANNOTATED && isBasePackage(object)) {
            resolveAnnotated(val);
        }
    }

    private void resolveString(Object model,String val,PropertyDescriptor descriptor) throws InvocationTargetException, IllegalAccessException {
        String filted = XssMessageFilter.filt(val);
        if(filted.equals(val)) {
            //若过滤之后没有变化，直接返回
            return;
        } else {
            //若过滤之后有变化，将变化后的值设置为该字段的值
            Method writter = descriptor.getWriteMethod();
            if( writter == null) {
                return;
            }
            writter.invoke(model,filted);
        }
    }

    /**
     * 处理集合类型的元素(对于集合中的String类型元素暂不处理)
     * @param collection
     */
    private void resolveCollection(Collection collection ) {
        collection.forEach(this::resolveCollectionItem);
    }

    private void resolveMap(Map map) {
        map.values().forEach(  this:: resolveCollectionItem);
    }


    /**
     * 处理集合中的元素
     * @param item
     */
    private void resolveCollectionItem(Object item) {
        if (item != null && isBasePackage(item)) {
            XssDefend xssDefend = AnnotationUtils.findAnnotation(item.getClass(),XssDefend.class);
            //若item对应的类型存在XssDefend注解，优先以该注解进行处理。若不存在，直接处理所有字段
            if(xssDefend == null || xssDefend.type() == XssDefendType.ALL) {
                resolveAll(item);
            } else {
                resolveAnnotated(item);
            }
        }
    }

    /**
     * 判断类型是否在basePackage下
     * @param object
     * @return
     */
    private boolean isBasePackage(Object object) {
        return object.getClass().getName().startsWith(basePackage);
    }

    /**
     * 是否可能需要进行xss处理
     * @param clazz
     * @return
     */
    private boolean isCandidateType(Class clazz) {
        return clazz == String.class
                || Collection.class.isAssignableFrom(clazz)
                || Map.class.isAssignableFrom(clazz)
                || clazz.getName().startsWith(basePackage);
    }
}

package com.yh.springextend.xss;

import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.serializer.SimpleDateFormatSerializer;
import com.alibaba.fastjson.support.config.FastJsonConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @ClassName XssDefendConfig
 * @Description Xss配置
 * @Author yh
 * @Date 2020-06-24 11:26
 * @Version 1.0
 */
@Configuration
@ConditionalOnProperty(name = "ad.xss.defend",havingValue = "true",matchIfMissing = true)
public class XssDefendConfig  extends WebMvcConfigurerAdapter {

    private static final String  dateFormat = "yyyy-MM-dd HH:mm:ss";

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        int jacksonIndex = converters.size();
        for(int i = 0;i<jacksonIndex;i++) {
            if( converters.get(i) instanceof MappingJackson2HttpMessageConverter) {
                jacksonIndex = i;
                break;
            }
        }
        converters.add(jacksonIndex,creageXssMessageConverter());
    }

    private XssFastJsonHttpMessageConverter creageXssMessageConverter() {
        XssFastJsonHttpMessageConverter fastJsonHttpMessageConverter = new XssFastJsonHttpMessageConverter("com.aisino");
        //添加fastJson的配置信息，比如：是否要格式化返回的json数据;
        FastJsonConfig fastJsonConfig = new FastJsonConfig();
        SerializeConfig serializeConfig = fastJsonConfig.getSerializeConfig();
        serializeConfig.put(Date.class, new SimpleDateFormatSerializer(dateFormat));
        fastJsonConfig.setSerializerFeatures(SerializerFeature.PrettyFormat);
        //fastJsonConfig.setSerializerFeatures(SerializerFeature.WriteNullStringAsEmpty);
        //处理中文乱码问题
        List<MediaType> fastMediaTypes = new ArrayList<>(1);
        fastMediaTypes.add(MediaType.ALL);
        //在convert中添加配置信息.
        fastJsonHttpMessageConverter.setSupportedMediaTypes(fastMediaTypes);
        fastJsonHttpMessageConverter.setFastJsonConfig(fastJsonConfig);
        return fastJsonHttpMessageConverter;
    }

}



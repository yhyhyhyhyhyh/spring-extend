package com.yh.springextend.swagger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import springfox.documentation.schema.TypeNameExtractor;
import springfox.documentation.spring.web.readers.parameter.ModelAttributeParameterExpander;

/**
 * 自定义构建Operation的插件托管给spring
 * @author yjx
 * @date 2019-08-06
 */
@Configuration
@ConditionalOnProperty(
        name = {"swagger.custom.plugin.enabled"}
)
public class CustomOperationPluginConfig {
    @Bean
    public CustomOperationReaderPlugin customOperationReaderPlugin() {
     return new CustomOperationReaderPlugin();
    }
    @Bean
    @Order(Ordered.LOWEST_PRECEDENCE)
    public CustomOperationBuilderPlugin customOperationBuilderPlugin(@Autowired(required = false) TypeNameExtractor nameExtractor) {
        return new CustomOperationBuilderPlugin(nameExtractor);
    }
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE+1)
    public CustomOperationParameterReader customOperationParameterReader(@Autowired ModelAttributeParameterExpander expander) {
        return new CustomOperationParameterReader(expander);
    }
}

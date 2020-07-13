package com.yh.springextend.swagger;


import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.classmate.TypeResolver;
import com.google.common.base.Optional;
import com.yh.springextend.swagger.annotation.CustomRequestModel;
import springfox.documentation.service.Parameter;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.OperationBuilderPlugin;
import springfox.documentation.spi.service.contexts.OperationContext;
import springfox.documentation.spring.web.readers.parameter.ExpansionContext;
import springfox.documentation.spring.web.readers.parameter.ModelAttributeParameterExpander;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toList;

/**
 * 自定义构建Operation的插件,解决项目中大量request.getParameter()方式获取参数时swagger原生注解过于臃肿，通过实体类集中管理
 * @author yh
 * @date 2019-08-06
 */
public class CustomOperationParameterReader implements OperationBuilderPlugin {

    private final ModelAttributeParameterExpander expander;

    public CustomOperationParameterReader(ModelAttributeParameterExpander expander) {
        this.expander = expander;
    }

    private TypeResolver typeResolver = new TypeResolver();

    @Override
    public void apply(OperationContext operationContext) {
        try {
            operationContext.operationBuilder().parameters(this.readParameters(operationContext));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean supports(DocumentationType documentationType) {
        return true;
    }

    private List<Parameter> readParameters(OperationContext context) throws Exception {
        List<Parameter> parameters = new ArrayList<>();
        Optional<CustomRequestModel> optExt =  context.findAnnotation(CustomRequestModel.class);
        if(optExt.isPresent()) {
            Class[] modelClasses = optExt.get().requestClass();
            if(modelClasses!=null && modelClasses.length>0) {
                for(Class clazz : modelClasses) {
                    ResolvedType resolvedType = typeResolver.resolve(clazz);
                    parameters.addAll(this.expander.expand(new ExpansionContext("",resolvedType, context)));
                }
            }
        }
        return parameters.stream().filter(((Predicate<Parameter>) Parameter::isHidden).negate()).collect(toList());
    }

}


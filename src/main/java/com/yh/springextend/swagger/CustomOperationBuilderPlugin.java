package com.yh.springextend.swagger;

import com.fasterxml.classmate.ResolvedType;
import com.google.common.base.Optional;
import com.yh.springextend.swagger.annotation.CustomResponseModel;
import org.springframework.beans.factory.annotation.Autowired;
import springfox.documentation.builders.ResponseMessageBuilder;
import springfox.documentation.schema.ModelReference;
import springfox.documentation.schema.TypeNameExtractor;
import springfox.documentation.service.ResponseMessage;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.schema.contexts.ModelContext;
import springfox.documentation.spi.service.OperationBuilderPlugin;
import springfox.documentation.spi.service.contexts.OperationContext;
import springfox.documentation.spring.web.readers.operation.ResponseMessagesReader;
import java.util.List;
import java.util.Map;
import static com.google.common.collect.Sets.newHashSet;
import static springfox.documentation.schema.ResolvedTypes.modelRefFactory;
import static springfox.documentation.schema.Types.isVoid;

/**
 * 自定义构建Operation的插件,解决项目中控制层返回的是字符串,swagger无法解析显示响应结果字段问题
 * @author yjx
 * @date 2019-08-06
 */
public class CustomOperationBuilderPlugin implements OperationBuilderPlugin {
    private final TypeNameExtractor nameExtractor;

    @Autowired
    public CustomOperationBuilderPlugin(TypeNameExtractor nameExtractor) {
        this.nameExtractor = nameExtractor;
    }
    @Override
    public void apply(OperationContext context) {
        ResolvedType returnType = context.getReturnType();
        returnType = setReslovedTypeByCutomAnnotation(returnType,context);
        if (returnType == null) {
            return;
        }
        returnType = context.alternateFor(returnType);
        ModelContext modelContext = ModelContext.returnValue(context.getGroupName(),
                returnType,
                context.getDocumentationType(),
                context.getAlternateTypeProvider(),
                context.getGenericsNamingStrategy(),
                context.getIgnorableParameterTypes());
        context.operationBuilder().responseModel(modelRefFactory(modelContext, nameExtractor).apply(returnType));
        List<ResponseMessage> responseMessages = context.getGlobalResponseMessages(context.httpMethod().toString());
        context.operationBuilder().responseMessages(newHashSet(responseMessages));
        applyReturnTypeOverride(context,returnType);
    }

    @Override
    public boolean supports(DocumentationType documentationType) {
        return documentationType.getVersion().equals(DocumentationType.SWAGGER_2.getVersion());
    }
    private void applyReturnTypeOverride(OperationContext context,ResolvedType returnType) {
        int httpStatusCode = ResponseMessagesReader.httpStatusCode(context);
        String message = ResponseMessagesReader.message(context);
        ModelReference modelRef = null;
        if (!isVoid(returnType)) {
            ModelContext modelContext = ModelContext.returnValue("default",returnType,
                    context.getDocumentationType(),
                    context.getAlternateTypeProvider(),
                    context.getGenericsNamingStrategy(),
                    context.getIgnorableParameterTypes());
            modelRef = modelRefFactory(modelContext, nameExtractor).apply(returnType);
        }
        ResponseMessage built = new ResponseMessageBuilder()
                .code(httpStatusCode)
                .message(message)
                .responseModel(modelRef)
                .build();
        context.operationBuilder().responseMessages(newHashSet(built));
    }
    private ResolvedType setReslovedTypeByCutomAnnotation(ResolvedType returnType,OperationContext context) {
        if (returnType.isInstanceOf(String.class) || returnType.isInstanceOf(Map.class)) {
            Optional<CustomResponseModel> customResponseModel = context.findAnnotation(CustomResponseModel.class);
            if (customResponseModel.equals(Optional.absent())) {
                return null;
            }
            returnType = ResolvedTypeUtils.setResolvedType(customResponseModel);
        }
        return returnType;
    }
}

package com.yh.springextend.swagger;

import com.fasterxml.classmate.ResolvedType;
import com.google.common.base.Optional;
import com.yh.springextend.swagger.annotation.CustomResponseModel;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.OperationModelsProviderPlugin;
import springfox.documentation.spi.service.contexts.RequestMappingContext;


/**
 * @author yjx
 * @date 2019-08-06
 */
public class CustomOperationReaderPlugin implements OperationModelsProviderPlugin {

    @Override
    public void apply(RequestMappingContext requestMappingContext) {
        Optional<CustomResponseModel> customResponseModel = requestMappingContext.findAnnotation(CustomResponseModel.class);
        if (customResponseModel.equals(Optional.absent())) {
            return;
        }
        Class classResponse = customResponseModel.get().classResponse();
        Class typeBinding = customResponseModel.get().typeBinding();
        if (classResponse.isAssignableFrom(Object.class) || typeBinding.isAssignableFrom(Object.class)) {
            return;
        }
        ResolvedType resolvedType = ResolvedTypeUtils.setResolvedType(customResponseModel);
        requestMappingContext.operationModelsBuilder().addReturn(resolvedType);
    }

    @Override
    public boolean supports(DocumentationType delimiter) {
        return delimiter.getVersion().equals(DocumentationType.SWAGGER_2.getVersion());
    }
}

package com.yh.springextend.swagger;

import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.classmate.TypeBindings;
import com.fasterxml.classmate.types.ResolvedObjectType;
import com.google.common.base.Optional;
import com.yh.springextend.swagger.annotation.CustomResponseModel;

import java.util.List;

/**
 * 基于自定义注解封装返回结果类型
 * @author yjx
 * @date 2019-08-06
 */
public class ResolvedTypeUtils {
    public static ResolvedType setResolvedType(Optional<CustomResponseModel> customResponseModel) {
        Class classResponse = customResponseModel.get().classResponse();
        Class typeBinding = customResponseModel.get().typeBinding();
        if (classResponse.isAssignableFrom(Object.class) || typeBinding.isAssignableFrom(Object.class)) {
            return null;
        }
        ResolvedType returnType = null;
        Class footListTypeBinding = customResponseModel.get().footListTypeBinding();
        Class queryListTypeBinding = customResponseModel.get().queryListTypeBinding();
        boolean footListIsObject = footListTypeBinding.isAssignableFrom(Object.class);
        boolean queryListIsObject = queryListTypeBinding.isAssignableFrom(Object.class);
        if (customResponseModel.get().isPage() && !typeBinding.isAssignableFrom(Object.class) && !typeBinding.isPrimitive() && !Constants.PACKAGE_LIST.contains(typeBinding.getName())) {
            ResolvedType footListTypeBindingType = null;
            ResolvedType queryListTypeBindingType = null;
            TypeBindings typeBindings = null;
            if (!footListIsObject) {
                ResolvedType footDtoBindType = new ResolvedObjectType(footListTypeBinding, TypeBindings.emptyBindings(), (ResolvedType) null, (List<ResolvedType>) null);
                footListTypeBindingType = new ResolvedObjectType(List.class, TypeBindings.create(List.class, new ResolvedType[]{footDtoBindType}), (ResolvedType) null, (List<ResolvedType>) null);
            }
            if (!queryListIsObject) {
                ResolvedType queryDtoBindType = new ResolvedObjectType(queryListTypeBinding, TypeBindings.emptyBindings(), (ResolvedType) null, (List<ResolvedType>) null);
                queryListTypeBindingType = new ResolvedObjectType(List.class, TypeBindings.create(List.class, new ResolvedType[]{queryDtoBindType}), (ResolvedType) null, (List<ResolvedType>) null);
            }
            if (!footListIsObject  && !queryListIsObject) {
                typeBindings = TypeBindings.create(typeBinding, new ResolvedType[]{queryListTypeBindingType,footListTypeBindingType});
            } else if (!queryListIsObject) {
                footListTypeBindingType = new ResolvedObjectType(Object.class, TypeBindings.emptyBindings(), (ResolvedType) null, (List<ResolvedType>) null);
                typeBindings = TypeBindings.create(typeBinding, new ResolvedType[]{queryListTypeBindingType,footListTypeBindingType});
            } else {
                queryListTypeBindingType = new ResolvedObjectType(Object.class, TypeBindings.emptyBindings(), (ResolvedType) null, (List<ResolvedType>) null);
                typeBindings = TypeBindings.create(typeBinding, new ResolvedType[]{queryListTypeBindingType,footListTypeBindingType});
            }
            ResolvedType resolvedBindType = new ResolvedObjectType(typeBinding,typeBindings , (ResolvedType) null, (List<ResolvedType>) null);
            returnType = new ResolvedObjectType(classResponse, TypeBindings.create(classResponse, new ResolvedType[]{resolvedBindType}), (ResolvedType) null, (List<ResolvedType>) null);
        } else {
            ResolvedType resolvedBindType = new ResolvedObjectType(typeBinding, TypeBindings.emptyBindings(), (ResolvedType) null, (List<ResolvedType>) null);
            returnType = new ResolvedObjectType(classResponse, TypeBindings.create(classResponse, new ResolvedType[]{resolvedBindType}), (ResolvedType) null, (List<ResolvedType>) null);
        }
        return returnType;
    }
}

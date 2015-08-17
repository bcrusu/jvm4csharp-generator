package com.jvm4csharp.generator.csharp;

import com.jvm4csharp.generator.reflectx.*;

import java.util.List;

public class CsType {
    public static final String IJavaObjectTypeName = "IJavaObject";
    public static final String jvm4csharpArrayUtilsNamespace = "jvm4csharp.ArrayUtils";

    public static void renderType(CsGenerationResult result, XType xType) {
        if (xType instanceof XClass) {
            XClass xClass = (XClass) xType;

            if (xClass.isVoid()) {
                result.append("void");
            } else if (xClass.isClass(Enum.class)) {
                result.append("Enum");
                result.addUsedNamespace("java.lang");
            } else if (xClass.isPrimitive()) {
                String csPrimitive;
                if (xClass.isClass(Boolean.TYPE))
                    csPrimitive = "bool";
                else if (xClass.isClass(Byte.TYPE))
                    csPrimitive = "byte";
                else if (xClass.isClass(Character.TYPE))
                    csPrimitive = "char";
                else if (xClass.isClass(Short.TYPE))
                    csPrimitive = "short";
                else if (xClass.isClass(Integer.TYPE))
                    csPrimitive = "int";
                else if (xClass.isClass(Long.TYPE))
                    csPrimitive = "long";
                else if (xClass.isClass(Float.TYPE))
                    csPrimitive = "float";
                else if (xClass.isClass(Double.TYPE))
                    csPrimitive = "double";
                else
                    throw new IllegalArgumentException(String.format("Unrecognized primitive type '%1s'.", xClass));

                result.append(csPrimitive);
            } else if (xClass.isArray()) {
                result.addUsedNamespace(jvm4csharpArrayUtilsNamespace);
                XClass elementType = xClass.getArrayComponentType();
                if (elementType.isPrimitive()) {
                    String csPrimitiveArray;
                    if (elementType.isClass(Boolean.TYPE))
                        csPrimitiveArray = "BooleanArray";
                    else if (elementType.isClass(Byte.TYPE))
                        csPrimitiveArray = "ByteArray";
                    else if (elementType.isClass(Character.TYPE))
                        csPrimitiveArray = "CharArray";
                    else if (elementType.isClass(Short.TYPE))
                        csPrimitiveArray = "ShortArray";
                    else if (elementType.isClass(Integer.TYPE))
                        csPrimitiveArray = "IntArray";
                    else if (elementType.isClass(Long.TYPE))
                        csPrimitiveArray = "LongArray";
                    else if (elementType.isClass(Float.TYPE))
                        csPrimitiveArray = "FloatArray";
                    else if (elementType.isClass(Double.TYPE))
                        csPrimitiveArray = "DoubleArray";
                    else
                        throw new IllegalArgumentException(String.format("Unrecognized primitive type '%1s'.", xClass));

                    result.append(csPrimitiveArray);
                } else {
                    result.append("ObjectArray<");
                    renderType(result, elementType);
                    result.append(">");
                }
            } else {
                renderSimpleTypeName(result, xClass);

                List<XTypeVariable> typeParameters = xClass.getTypeParameters();
                if (typeParameters.size() > 0) {
                    result.append("<");
                    for (int i = 0; i < typeParameters.size(); i++) {
                        result.append(IJavaObjectTypeName);

                        if (i < typeParameters.size() - 1)
                            result.append(", ");
                    }
                    result.append(">");
                }
            }
        } else if (xType instanceof XTypeVariable) {
            XTypeVariable xTypeVariable = (XTypeVariable) xType;
            XType resolvedType = xTypeVariable.getResolvedType();

            if (resolvedType instanceof XTypeVariable) {
                result.append(((XTypeVariable) resolvedType).getName());
            } else {
                renderType(result, resolvedType);
            }
        } else if (xType instanceof XParameterizedType) {
            XParameterizedType xParameterizedType = (XParameterizedType) xType;
            List<XType> actualTypeArguments = xParameterizedType.getActualTypeArguments();

            renderSimpleTypeName(result, xParameterizedType.getRawType());
            result.append("<");
            for (int i = 0; i < actualTypeArguments.size(); i++) {
                XType actualTypeArgument = actualTypeArguments.get(i);

                renderType(result, actualTypeArgument);
                if (i < actualTypeArguments.size() - 1)
                    result.append(", ");
            }
            result.append(">");
        } else if (xType instanceof XWildcardType) {
            XWildcardType xWildcardType = (XWildcardType) xType;

            if (xWildcardType.getUpperBound() != null)
                renderType(result, xWildcardType.getUpperBound());
            else {
                // C# doesn't have a similar feature for lower bounds; will default to Object
                result.append(IJavaObjectTypeName);
            }
        } else if (xType instanceof XGenericArrayType) {
            XGenericArrayType xGenericArrayType = (XGenericArrayType) xType;
            XType genericComponentType = xGenericArrayType.getGenericComponentType();

            result.append("ObjectArray<");
            renderType(result, genericComponentType);
            result.append(">");
            result.addUsedNamespace(jvm4csharpArrayUtilsNamespace);
        } else
            throw new IllegalArgumentException(String.format("Unrecognized type '%1s'.", xType));
    }

    public static void renderSimpleTypeName(CsGenerationResult result, XClass xClass) {
        if (xClass.isClass(Object.class)) {
            result.append(IJavaObjectTypeName);
            return;
        }

        String className = xClass.getSimpleName();
        XClass declaringClass = xClass.getDeclaringClass();
        while (declaringClass != null) {
            className = "." + className;
            if (declaringClass.isInterface())
                className = "_" + className;

            className = declaringClass.getSimpleName() + className;
            declaringClass = declaringClass.getDeclaringClass();
        }

        result.append(CsTemplateHelper.escapeCsKeyword(className));
        result.addUsedNamespace(getNamespace(xClass));
    }

    public static void renderSimpleTypeName(CsGenerationResult result, XClassDefinition classDefinition) {
        renderSimpleTypeName(result, classDefinition.getXClass());
    }

    public static String getNamespace(XClass xClass) {
        return getNamespace(xClass.getPackageName());
    }

    public static String getNamespace(String packageName) {
        StringBuilder sb = new StringBuilder();
        String[] splits = packageName.split("\\.");

        for (int i = 0; i < splits.length; i++) {
            sb.append(CsTemplateHelper.escapeCsKeyword(splits[i]));

            if (i < splits.length - 1)
                sb.append(".");
        }

        return sb.toString();
    }
}

package com.jvm4csharp.generator.csharp;

import com.jvm4csharp.generator.reflectx.*;

import java.util.List;

public class CsType {
    public static final String IJavaObjectTypeName = "IJavaObject";
    public static final String jvm4csharpArrayUtilsNamespace = "jvm4csharp.ArrayUtils";

    public static void renderUnboundType(CsGenerationResult result, XClassDefinition classDefinition) {
        renderSimpleTypeName(result, classDefinition);

        int typeParametersCount = classDefinition.getTypeParameters().size();
        if (typeParametersCount > 0) {
            result.append("<");
            for (int i = 0; i < typeParametersCount - 1; i++)
                result.append(",");
            result.append(">");
        }
    }

    public static void renderTypeDefinition(CsGenerationResult result, XClassDefinition classDefinition) {
        renderSimpleTypeName(result, classDefinition);

        List<XType> actualTypeArguments = classDefinition.getActualTypeArguments();
        if (actualTypeArguments.size() > 0) {
            result.append("<");
            for (int i = 0; i < actualTypeArguments.size(); i++) {
                XType actualTypeArgument = actualTypeArguments.get(i);
                renderType(result, actualTypeArgument);
                if (i < actualTypeArguments.size() - 1)
                    result.append(", ");
            }
            result.append(">");
        }
    }

    public static void renderType(CsGenerationResult result, XType xType) {
        if (xType instanceof XClass) {
            XClass xClass = (XClass) xType;

            if (xClass.isVoid()) {
                result.append("void");
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
            // C# doesn't have a similar feature; will default to Object
            result.append(IJavaObjectTypeName);
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

    public static void renderErasedType(CsGenerationResult result, XType xType) {
        if (xType instanceof XClass) {
            XClass xClass = (XClass) xType;

            if (xClass.isVoid() || xClass.isPrimitive() || xClass.isArray()) {
                renderType(result, xType);
            } else {
                renderSimpleTypeName(result, xClass);

                List<XTypeVariable> typeParameters = xClass.getTypeParameters();
                if (typeParameters.size() > 0) {
                    result.append("<");
                    for (int i = 0; i < typeParameters.size(); i++) {
                        renderErasedType(result, typeParameters.get(i));

                        if (i < typeParameters.size() - 1)
                            result.append(", ");
                    }
                    result.append(">");
                }
            }
        } else if (xType instanceof XTypeVariable) {
            XTypeVariable xTypeVariable = (XTypeVariable) xType;
            XType resolvedType = xTypeVariable.getResolvedType();

            if (!(resolvedType instanceof XTypeVariable)) {
                renderType(result, resolvedType);
            } else {
                List<XType> bounds = ((XTypeVariable) resolvedType).getBounds();
                if (bounds.size() > 1)
                    throw new IllegalArgumentException(String.format("Cannot render erased type for type variable with multiple bounds: '%1s'", resolvedType));

                if (bounds.size() == 0) {
                    result.append(IJavaObjectTypeName);
                    return;
                }

                renderErasedType(result, bounds.get(0));
            }
        } else if (xType instanceof XParameterizedType) {
            XParameterizedType xParameterizedType = (XParameterizedType) xType;
            renderSimpleTypeName(result, xParameterizedType.getRawType());
            return;
        } else if (xType instanceof XWildcardType) {
            // C# doesn't have a similar feature; will default to Object
            //TODO: use upper bounds
            result.append(IJavaObjectTypeName);
        } else if (xType instanceof XGenericArrayType) {
            XGenericArrayType xGenericArrayType = (XGenericArrayType) xType;
            XType genericComponentType = xGenericArrayType.getGenericComponentType();

            result.append("ObjectArray<");
            renderErasedType(result, genericComponentType);
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
            className = declaringClass.getSimpleName() + "_" + className;
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

    //TODO: remove
    public static String getSimpleTypeName(XClass xClass) {
        CsGenerationResult tmpResult = new CsGenerationResult();
        renderSimpleTypeName(tmpResult, xClass);
        return tmpResult.toString().trim();
    }
}

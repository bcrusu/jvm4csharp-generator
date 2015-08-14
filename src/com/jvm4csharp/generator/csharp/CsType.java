package com.jvm4csharp.generator.csharp;

import com.jvm4csharp.generator.reflectx.*;

import java.util.List;

public class CsType {
    public static final String IJavaObjectTypeName = "IJavaObject";

    public static String renderUnboundType(XClassDefinition classDefinition) {
        StringBuilder sb = new StringBuilder();
        sb.append(renderSimpleTypeName(classDefinition));

        int typeParametersCount = classDefinition.getTypeParameters().size();
        if (typeParametersCount > 0) {
            sb.append("<");
            for (int i = 0; i < typeParametersCount - 1; i++)
                sb.append(",");
            sb.append(">");
        }

        return sb.toString();
    }

    public static String renderTypeDefinition(XClassDefinition classDefinition) {
        StringBuilder sb = new StringBuilder();
        sb.append(renderSimpleTypeName(classDefinition));

        List<XType> actualTypeArguments = classDefinition.getActualTypeArguments();
        if (actualTypeArguments.size() > 0) {
            sb.append("<");
            for (int i = 0; i < actualTypeArguments.size(); i++) {
                XType actualTypeArgument = actualTypeArguments.get(i);
                sb.append(renderType(actualTypeArgument));
                if (i < actualTypeArguments.size() - 1)
                    sb.append(", ");
            }
            sb.append(">");
        }

        return sb.toString();
    }

    public static String renderType(XType xType) {
        if (xType instanceof XClass) {
            XClass xClass = (XClass) xType;

            if (xClass.isVoid()) {
                return "void";
            }

            if (xClass.isPrimitive()) {
                if (xClass.isClass(Boolean.TYPE))
                    return "bool";
                else if (xClass.isClass(Byte.TYPE))
                    return "byte";
                else if (xClass.isClass(Character.TYPE))
                    return "char";
                else if (xClass.isClass(Short.TYPE))
                    return "short";
                else if (xClass.isClass(Integer.TYPE))
                    return "int";
                else if (xClass.isClass(Long.TYPE))
                    return "long";
                else if (xClass.isClass(Float.TYPE))
                    return "float";
                else if (xClass.isClass(Double.TYPE))
                    return "double";
                else
                    throw new IllegalArgumentException(String.format("Unrecognized primitive type '%1s'.", xClass));
            }

            if (xClass.isArray()) {
                XClass elementType = xClass.getArrayComponentType();

                if (elementType.isClass(Boolean.TYPE))
                    return "BooleanArray";
                else if (elementType.isClass(Byte.TYPE))
                    return "ByteArray";
                else if (elementType.isClass(Character.TYPE))
                    return "CharArray";
                else if (elementType.isClass(Short.TYPE))
                    return "ShortArray";
                else if (elementType.isClass(Integer.TYPE))
                    return "IntArray";
                else if (elementType.isClass(Long.TYPE))
                    return "LongArray";
                else if (elementType.isClass(Float.TYPE))
                    return "FloatArray";
                else if (elementType.isClass(Double.TYPE))
                    return "DoubleArray";
                else
                    return "ObjectArray<" + renderType(elementType) + ">";
            }

            StringBuilder sb = new StringBuilder();
            sb.append(renderSimpleTypeName(xClass));

            List<XTypeVariable> typeParameters = xClass.getTypeParameters();
            if (typeParameters.size() > 0) {
                sb.append("<");
                for (int i = 0; i < typeParameters.size(); i++) {
                    sb.append(IJavaObjectTypeName);

                    if (i < typeParameters.size() - 1)
                        sb.append(", ");
                }
                sb.append(">");
            }

            return sb.toString();
        } else if (xType instanceof XTypeVariable) {
            XTypeVariable xTypeVariable = (XTypeVariable) xType;
            XType resolvedType = xTypeVariable.getResolvedType();

            if (resolvedType instanceof XTypeVariable)
                return ((XTypeVariable) resolvedType).getName();

            return renderType(resolvedType);
        } else if (xType instanceof XParameterizedType) {
            XParameterizedType xParameterizedType = (XParameterizedType) xType;
            List<XType> actualTypeArguments = xParameterizedType.getActualTypeArguments();

            StringBuilder sb = new StringBuilder();
            sb.append(renderSimpleTypeName(xParameterizedType.getRawType()));
            sb.append("<");
            for (int i = 0; i < actualTypeArguments.size(); i++) {
                XType actualTypeArgument = actualTypeArguments.get(i);

                sb.append(renderType(actualTypeArgument));
                if (i < actualTypeArguments.size() - 1)
                    sb.append(", ");
            }
            sb.append(">");

            return sb.toString();
        } else if (xType instanceof XWildcardType) {
            // C# doesn't have a similar feature; will default to Object
            return IJavaObjectTypeName;
        } else if (xType instanceof XGenericArrayType) {
            XGenericArrayType xGenericArrayType = (XGenericArrayType) xType;
            XType genericComponentType = xGenericArrayType.getGenericComponentType();

            return "ObjectArray<" + renderType(genericComponentType) + ">";
        }

        throw new IllegalArgumentException(String.format("Unrecognized type '%1s'.", xType));
    }

    public static String renderErasedType(XType xType) {
        if (xType instanceof XClass) {
            XClass xClass = (XClass) xType;

            if (xClass.isVoid() || xClass.isPrimitive() || xClass.isArray())
                return renderType(xType);

            StringBuilder sb = new StringBuilder();
            sb.append(renderSimpleTypeName(xClass));

            List<XTypeVariable> typeParameters = xClass.getTypeParameters();
            if (typeParameters.size() > 0) {
                sb.append("<");
                for (int i = 0; i < typeParameters.size(); i++) {
                    sb.append(renderErasedType(typeParameters.get(i)));

                    if (i < typeParameters.size() - 1)
                        sb.append(", ");
                }
                sb.append(">");
            }

            return sb.toString();
        } else if (xType instanceof XTypeVariable) {
            XTypeVariable xTypeVariable = (XTypeVariable) xType;
            XType resolvedType = xTypeVariable.getResolvedType();

            if (!(resolvedType instanceof XTypeVariable))
                return renderType(resolvedType);

            List<XType> bounds = ((XTypeVariable) resolvedType).getBounds();
            if (bounds.size() > 1)
                throw new IllegalArgumentException(String.format("Cannot render erased type for type variable with multiple bounds: '%1s'", resolvedType));

            if (bounds.size() == 0)
                return IJavaObjectTypeName;

            return renderErasedType(bounds.get(0));
        } else if (xType instanceof XParameterizedType) {
            XParameterizedType xParameterizedType = (XParameterizedType) xType;
            return renderSimpleTypeName(xParameterizedType.getRawType());
        } else if (xType instanceof XWildcardType) {
            // C# doesn't have a similar feature; will default to Object
            return IJavaObjectTypeName;
        } else if (xType instanceof XGenericArrayType) {
            XGenericArrayType xGenericArrayType = (XGenericArrayType) xType;
            XType genericComponentType = xGenericArrayType.getGenericComponentType();
            return "ObjectArray<" + renderErasedType(genericComponentType) + ">";
        }

        throw new IllegalArgumentException(String.format("Unrecognized type '%1s'.", xType));
    }

    public static String renderSimpleTypeName(XClass xClass) {
        if (xClass.isClass(Object.class))
            return IJavaObjectTypeName;

        String result = xClass.getSimpleName();

        XClass declaringClass = xClass.getDeclaringClass();
        while (declaringClass != null) {
            result = declaringClass.getSimpleName() + "_" + result;
            declaringClass = declaringClass.getDeclaringClass();
        }

        return CsTemplateHelper.escapeCsKeyword(result);
    }

    public static String renderSimpleTypeName(XClassDefinition classDefinition) {
        return renderSimpleTypeName(classDefinition.getXClass());
    }

    public static String renderNamespace(XClass xClass) {
        return renderNamespace(xClass.getPackageName());
    }

    public static String renderNamespace(String packageName) {
        String[] splits = packageName.split("\\.");

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < splits.length; i++) {
            sb.append(CsTemplateHelper.escapeCsKeyword(splits[i]));

            if (i < splits.length - 1)
                sb.append(".");
        }

        return sb.toString();
    }
}

package com.jvm4csharp.generator.csharp;

import com.jvm4csharp.generator.reflectx.*;

import java.util.List;

public class CsType {
    public static String getDisplayName(XClassDefinition xClassDefinition) {
        StringBuilder sb = new StringBuilder();
        sb.append(getSimpleClassName(xClassDefinition.getXClass()));

        List<XType> actualTypeArguments = xClassDefinition.getActualTypeArguments();
        if (actualTypeArguments.size() > 0) {
            sb.append("<");
            for (int i = 0; i < actualTypeArguments.size(); i++) {
                XType actualTypeArgument = actualTypeArguments.get(i);
                sb.append(getDisplayName(actualTypeArgument));
                if (i < actualTypeArguments.size() - 1)
                    sb.append(", ");
            }
            sb.append(">");
        }

        return sb.toString();
    }

    public static String getDisplayName(XType xType) {
        if (xType instanceof XClass) {
            XClass xClass = (XClass) xType;

            if (xClass.isClass(Void.TYPE)) {
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
                    return "ObjectArray<" + getDisplayName(elementType) + ">";
            }

            StringBuilder sb = new StringBuilder();
            sb.append(getSimpleClassName(xClass));

            List<XTypeVariable> typeParameters = xClass.getTypeParameters();
            if (typeParameters.size() > 0) {
                sb.append("<");
                for (int i = 0; i < typeParameters.size(); i++) {
                    sb.append("IJavaObject");

                    if (i < typeParameters.size() - 1)
                        sb.append(", ");
                }
                sb.append(">");
            }

            return sb.toString();
        } else if (xType instanceof XTypeVariable) {
            //TODO: handle bounds - C# 'where' type param constraints
            XTypeVariable xTypeVariable = (XTypeVariable) xType;
            return xTypeVariable.getName();
        } else if (xType instanceof XParameterizedType) {
            XParameterizedType xParameterizedType = (XParameterizedType) xType;
            XClass rawType = xParameterizedType.getRawType();

            List<XType> actualTypeArguments = xParameterizedType.getActualTypeArguments();

            StringBuilder sb = new StringBuilder();
            sb.append(rawType.getSimpleName());
            sb.append("<");
            for (int i = 0; i < actualTypeArguments.size(); i++) {
                XType actualTypeArgument = actualTypeArguments.get(i);

                sb.append(getDisplayName(actualTypeArgument));
                if (i < actualTypeArguments.size() - 1)
                    sb.append(", ");
            }
            sb.append(">");

            return sb.toString();
        } else if (xType instanceof XWildcardType) {
            // C# doesn't have a similar feature; will default to Object
            return "IJavaObject";
        } else if (xType instanceof XGenericArrayType) {
            XGenericArrayType xGenericArrayType = (XGenericArrayType) xType;
            XType genericComponentType = xGenericArrayType.getGenericComponentType();

            return "ObjectArray<" + getDisplayName(genericComponentType) + ">";
        }

        throw new IllegalArgumentException(String.format("Unrecognized type '%1s'.", xType));
    }

    public static String getSimpleClassName(XClass xClass) {
        String result = xClass.getSimpleName();

        XClass declaringClass = xClass.getDeclaringClass();
        while (declaringClass != null) {
            result = declaringClass.getSimpleName() + "_" + result;
            declaringClass = declaringClass.getDeclaringClass();
        }

        return CsTemplateHelper.escapeCsKeyword(result);
    }

    public static String getNamespace(XClass xClass) {
        return getNamespace(xClass.getPackageName());
    }

    public static String getNamespace(String packageName) {
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

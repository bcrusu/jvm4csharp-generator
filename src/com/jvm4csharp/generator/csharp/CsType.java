package com.jvm4csharp.generator.csharp;

import java.lang.reflect.*;
import java.util.HashSet;
import java.util.Set;

public final class CsType {
    public String displayName;
    public Set<String> namespacesUsed;

    public CsType() {
        namespacesUsed = new HashSet<>();
    }

    public static CsType getCsType(Type type) {
        if (Class.class.isAssignableFrom(type.getClass())) {
            Class clazz = (Class) type;

            if (clazz.isSynthetic() || clazz.isLocalClass() || clazz.isAnonymousClass()) {
                throw new Error("Not supported");
            }

            if (clazz == Void.TYPE) {
                CsType result = new CsType();
                result.displayName = "void";
                return result;
            }

            if (clazz.isPrimitive()) {
                CsType result = new CsType();

                if (clazz == Boolean.TYPE)
                    result.displayName = "bool";
                else if (clazz == Byte.TYPE)
                    result.displayName = "byte";
                else if (clazz == Character.TYPE)
                    result.displayName = "char";
                else if (clazz == Short.TYPE)
                    result.displayName = "short";
                else if (clazz == Integer.TYPE)
                    result.displayName = "int";
                else if (clazz == Long.TYPE)
                    result.displayName = "long";
                else if (clazz == Float.TYPE)
                    result.displayName = "float";
                else if (clazz == Double.TYPE)
                    result.displayName = "double";
                else
                    throw new IllegalArgumentException(String.format("Unrecognized primitive type '%1s'.", clazz));
                return result;
            }

            if (clazz.isArray()) {
                CsType result = new CsType();
                Class elementType = clazz.getComponentType();
                result.namespacesUsed.add("jvm4csharp.ArrayUtils");

                if (elementType == Boolean.TYPE)
                    result.displayName = "BooleanArray";
                else if (elementType == Byte.TYPE)
                    result.displayName = "ByteArray";
                else if (elementType == Character.TYPE)
                    result.displayName = "CharArray";
                else if (elementType == Short.TYPE)
                    result.displayName = "ShortArray";
                else if (elementType == Integer.TYPE)
                    result.displayName = "IntArray";
                else if (elementType == Long.TYPE)
                    result.displayName = "LongArray";
                else if (elementType == Float.TYPE)
                    result.displayName = "FloatArray";
                else if (elementType == Float.TYPE)
                    result.displayName = "DoubleArray";
                else {
                    CsType elementCsType = getCsType(elementType);
                    result.displayName = "ObjectArray<" + elementCsType.displayName + ">";
                    result.namespacesUsed.addAll(elementCsType.namespacesUsed);
                }

                return result;
            }

            CsType result = new CsType();
            result.displayName = getCsClassName(clazz);
            result.namespacesUsed.add(clazz.getPackage().getName());
            return result;
        } else if (TypeVariable.class.isAssignableFrom(type.getClass())) {
            //TODO: handle bounds (C# 'where' type param constraints
            TypeVariable typeVariable = (TypeVariable) type;
            CsType result = new CsType();
            result.displayName = typeVariable.getName();
            return result;
        } else if (ParameterizedType.class.isAssignableFrom(type.getClass())) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            Type rawType = parameterizedType.getRawType();
            CsType rawTypeCsType = getCsType(rawType);

            CsType result = new CsType();
            result.namespacesUsed.addAll(rawTypeCsType.namespacesUsed);

            Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();

            StringBuilder sb = new StringBuilder();
            sb.append(rawTypeCsType.displayName);
            sb.append("<");
            for (int i = 0; i < actualTypeArguments.length; i++) {
                Type actualTypeArgument = actualTypeArguments[i];
                CsType csType = getCsType(actualTypeArgument);
                result.namespacesUsed.addAll(csType.namespacesUsed);

                sb.append(csType.displayName);
                if (i < actualTypeArguments.length - 1)
                    sb.append(", ");
            }
            sb.append(">");

            result.displayName = sb.toString();
            return result;
        } else if (WildcardType.class.isAssignableFrom(type.getClass())) {
            WildcardType wildcardType = (WildcardType) type;
            Type[] lowerBounds = wildcardType.getLowerBounds();
            Type[] upperBounds = wildcardType.getUpperBounds();

            lowerBounds = lowerBounds != null ? lowerBounds : new Type[0];
            upperBounds = upperBounds != null ? upperBounds : new Type[0];

            // extra checks won't hurt; the API doesn't look too friendly to strangers
            if (lowerBounds.length > 1)
                throw new IllegalArgumentException("Invalid WildcardType detected. Too many lower bounds.");
            if (upperBounds.length > 1)
                throw new IllegalArgumentException("Invalid WildcardType detected. Too many upper bounds.");

            // C# doesn't have a similar feature; will default to Object
            CsType result = new CsType();
            result.displayName = "IJavaObject";
            return result;
        } else if (GenericArrayType.class.isAssignableFrom(type.getClass())) {
            GenericArrayType genericArrayType = (GenericArrayType) type;
            Type genericComponentType = genericArrayType.getGenericComponentType();
            CsType genericComponentCsType = getCsType(genericComponentType);

            CsType result = new CsType();
            result.displayName = "ObjectArray<" + genericComponentCsType.displayName + ">";
            result.namespacesUsed.addAll(genericComponentCsType.namespacesUsed);
            result.namespacesUsed.add("jvm4csharp.ArrayUtils");
            return result;
        }

        throw new IllegalArgumentException(String.format("Unrecognized type '%1s'.", type));
    }

    public static String getCsClassName(Class clazz){
        String result = clazz.getSimpleName();

        Class declaringClass = clazz.getDeclaringClass();
        while (declaringClass != null){
            result = declaringClass.getSimpleName() + "_" + result;
            declaringClass = declaringClass.getDeclaringClass();
        }

        return result;
    }
}

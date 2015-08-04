package com.jvm4csharp.generator.csharp;

public final class CsConverter {
    public static CsType GetClrType(Class clazz) {
        if (clazz.isPrimitive()) {
            CsType result = new CsType();

            if (clazz == Boolean.TYPE)
                result.DisplayName = "bool";
            else if (clazz == Byte.TYPE)
                result.DisplayName = "byte";
            else if (clazz == Character.TYPE)
                result.DisplayName = "char";
            else if (clazz == Short.TYPE)
                result.DisplayName = "short";
            else if (clazz == Integer.TYPE)
                result.DisplayName = "int";
            else if (clazz == Long.TYPE)
                result.DisplayName = "long";
            else if (clazz == Float.TYPE)
                result.DisplayName = "float";
            else if (clazz == Float.TYPE)
                result.DisplayName = "double";
            else
                throw new IllegalArgumentException("Unrecognized primitive type");
            return result;
        }

        if (clazz.isArray()) {
            CsType result = new CsType();
            Class elementType = clazz.getComponentType();
            result.NamespacesUsed.add("jvm4csharp.ArrayUtils");

            if (elementType == Boolean.TYPE)
                result.DisplayName = "BooleanArray";
            if (elementType == Byte.TYPE)
                result.DisplayName = "ByteArray";
            if (elementType == Character.TYPE)
                result.DisplayName = "CharArray";
            if (elementType == Short.TYPE)
                result.DisplayName = "ShortArray";
            if (elementType == Integer.TYPE)
                result.DisplayName = "IntArray";
            if (elementType == Long.TYPE)
                result.DisplayName = "LongArray";
            if (elementType == Float.TYPE)
                result.DisplayName = "FloatArray";
            if (elementType == Float.TYPE)
                result.DisplayName = "DoubleArray";
            else {
                CsType elementCsType = GetClrType(elementType);
                result.DisplayName = "ObjectArray<" + elementCsType.DisplayName + ">";
                result.NamespacesUsed.addAll(elementCsType.NamespacesUsed);
            }

            return result;
        }

        if (clazz.isEnum()) {
            throw new Error(); //TODO
        }

        if (clazz.isSynthetic() || clazz.isLocalClass() || clazz.isAnonymousClass()) {
            throw new Error("Not supported");
        }

        //TODO: generics
        //ParameterizedType parameterizedType = (ParameterizedType)getClass()                .getGenericSuperclass();
        //return (Class) parameterizedType.getActualTypeArguments()[0];

        String packageName = clazz.getPackage().getName();

        CsType result = new CsType();
        result.DisplayName = clazz.getCanonicalName().substring(packageName.length()+ 1);
        result.NamespacesUsed.add("jvm4csharp." + packageName);
        return result;
    }
}

package com.jvm4csharp.generator.csharp;

public final class CsConverter {
    public static CsType GetClrType(Class clazz) {
        if (clazz == Void.TYPE){
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
            else if (clazz == Float.TYPE)
                result.displayName = "double";
            else
                throw new IllegalArgumentException("Unrecognized primitive type");
            return result;
        }

        if (clazz.isArray()) {
            CsType result = new CsType();
            Class elementType = clazz.getComponentType();
            result.namespacesUsed.add("jvm4csharp.ArrayUtils");

            if (elementType == Boolean.TYPE)
                result.displayName = "BooleanArray";
            if (elementType == Byte.TYPE)
                result.displayName = "ByteArray";
            if (elementType == Character.TYPE)
                result.displayName = "CharArray";
            if (elementType == Short.TYPE)
                result.displayName = "ShortArray";
            if (elementType == Integer.TYPE)
                result.displayName = "IntArray";
            if (elementType == Long.TYPE)
                result.displayName = "LongArray";
            if (elementType == Float.TYPE)
                result.displayName = "FloatArray";
            if (elementType == Float.TYPE)
                result.displayName = "DoubleArray";
            else {
                CsType elementCsType = GetClrType(elementType);
                result.displayName = "ObjectArray<" + elementCsType.displayName + ">";
                result.namespacesUsed.addAll(elementCsType.namespacesUsed);
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
        result.displayName = clazz.getCanonicalName().substring(packageName.length()+ 1);
        result.namespacesUsed.add("jvm4csharp." + packageName);
        return result;
    }
}

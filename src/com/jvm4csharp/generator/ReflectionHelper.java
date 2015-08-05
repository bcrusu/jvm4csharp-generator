package com.jvm4csharp.generator;

import java.lang.reflect.*;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class ReflectionHelper {
    public static boolean isPublic(int modifier) {
        return Modifier.isPublic(modifier);
    }

    public static boolean isFinal(int modifier) {
        return Modifier.isFinal(modifier);
    }

    public static boolean isStatic(int modifier) {
        return Modifier.isStatic(modifier);
    }

    public static boolean isPublic(Member member) {
        return isPublic(member.getModifiers());
    }

    public static boolean isPublic(Class clazz) {
        return isPublic(clazz.getModifiers());
    }

    public static boolean isFinal(Member member) {
        return isFinal(member.getModifiers());
    }

    public static boolean isFinal(Class clazz) {
        return isFinal(clazz.getModifiers());
    }

    public static boolean isStatic(Member member) {
        return isStatic(member.getModifiers());
    }

    public static boolean isStatic(Class clazz) {
        return isStatic(clazz.getModifiers());
    }

    public static List<Field> getPublicDeclaredFields(Class clazz) {
        return Arrays.asList(clazz.getDeclaredFields())
                .stream()
                .filter(x -> isPublic(x))
                .collect(Collectors.toList());
    }

    public static List<Method> getPublicDeclaredMethods(Class clazz) {
        return Arrays.asList(clazz.getDeclaredMethods())
                .stream()
                .filter(x -> isPublic(x))
                .collect(Collectors.toList());
    }

    public static List<Constructor> getPublicDeclaredConstructors(Class clazz) {
        return Arrays.asList(clazz.getDeclaredConstructors())
                .stream()
                .filter(x -> isPublic(x))
                .collect(Collectors.toList());
    }

    public static List<Class> getPublicDeclaredClasses(Class clazz) {
        return Arrays.asList(clazz.getDeclaredClasses())
                .stream()
                .filter(x -> isPublic(x))
                .collect(Collectors.toList());
    }

    public static List<Class> getPublicImplementedInterfaces(Class clazz) {
        return Arrays.asList(clazz.getInterfaces())
                .stream()
                .filter(x -> isPublic(x))
                .collect(Collectors.toList());
    }

    public static String GetInternalTypeName(Class clazz) {
        String result;
        if (clazz == Void.TYPE) {
            return "V";
        }

        if (clazz.isPrimitive()) {
            if (clazz == Boolean.TYPE)
                result = "Z";
            else if (clazz == Byte.TYPE)
                result = "B";
            else if (clazz == Character.TYPE)
                result = "C";
            else if (clazz == Short.TYPE)
                result = "S";
            else if (clazz == Integer.TYPE)
                result = "I";
            else if (clazz == Long.TYPE)
                result = "J";
            else if (clazz == Float.TYPE)
                result = "F";
            else if (clazz == Float.TYPE)
                result = "D";
            else
                throw new Error("Unrecognized primitive type.");
        } else {
            result = clazz.getCanonicalName();
            result = result.replace('.', '/');
            result = 'L' + result + ';';
        }

        if (clazz.isArray())
            result = '[' + result;

        return result;
    }

    public static String GetInternalSignature(Method method) {
        StringBuilder result = new StringBuilder();
        result.append('(');

        Class[] parameterTypes = method.getParameterTypes();
        for (Class parameterType : parameterTypes)
            result.append(GetInternalTypeName(parameterType));

        result.append(')');
        result.append(GetInternalTypeName(method.getReturnType()));

        return result.toString();
    }

    public static String GetInternalSignature(Constructor constructor) {
        StringBuilder result = new StringBuilder();
        result.append('(');

        Class[] parameterTypes = constructor.getParameterTypes();
        for (Class parameterType : parameterTypes)
            result.append(GetInternalTypeName(parameterType));

        result.append(")V");

        return result.toString();
    }
}

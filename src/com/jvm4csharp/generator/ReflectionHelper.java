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
//TODO:
    public static String GetInternalTypeName(Class clazz){
        if (clazz.isPrimitive()){
            if (clazz == Boolean.TYPE)
                return "Z";
            if (clazz == Byte.TYPE)
                return "B";
            if (clazz == Character.TYPE)
                return "C";
            if (clazz == Short.TYPE)
                return "S";
            if (clazz == Integer.TYPE)
                return "I";
            if (clazz == Long.TYPE)
                return "J";
            if (clazz == Float.TYPE)
                return "F";
            if (clazz == Float.TYPE)
                return "D";
        }
        else if (clazz.isArray()){

        }

        throw new IllegalArgumentException();
    }
}

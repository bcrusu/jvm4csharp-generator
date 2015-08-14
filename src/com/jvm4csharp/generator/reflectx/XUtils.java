package com.jvm4csharp.generator.reflectx;

import java.lang.reflect.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

class XUtils {
    static boolean isPublic(int modifier) {
        return Modifier.isPublic(modifier);
    }

    static boolean isFinal(int modifier) {
        return Modifier.isFinal(modifier);
    }

    static boolean isStatic(int modifier) {
        return Modifier.isStatic(modifier);
    }

    static boolean isAbstract(int modifier) {
        return Modifier.isAbstract(modifier);
    }

    static boolean isPublic(Member member) {
        return isPublic(member.getModifiers());
    }

    static boolean isPublic(Class clazz) {
        return isPublic(clazz.getModifiers());
    }

    static boolean isFinal(Member member) {
        return isFinal(member.getModifiers());
    }

    static boolean isFinal(Class clazz) {
        return isFinal(clazz.getModifiers());
    }

    static boolean isStatic(Member member) {
        return isStatic(member.getModifiers());
    }

    static boolean isStatic(Class clazz) {
        return isStatic(clazz.getModifiers());
    }

    static boolean isAbstract(Member member) {
        return isAbstract(member.getModifiers());
    }

    static boolean isAbstract(Class clazz) {
        return Modifier.isAbstract(clazz.getModifiers());
    }

    static String getInternalTypeName(Class clazz) {
        if (clazz == Void.TYPE) {
            return "V";
        }

        String result = "";
        if (clazz.isArray()) {
            result = "[";
            clazz = clazz.getComponentType();
        }

        if (clazz.isPrimitive()) {
            if (clazz == Boolean.TYPE)
                result += "Z";
            else if (clazz == Byte.TYPE)
                result += "B";
            else if (clazz == Character.TYPE)
                result += "C";
            else if (clazz == Short.TYPE)
                result += "S";
            else if (clazz == Integer.TYPE)
                result += "I";
            else if (clazz == Long.TYPE)
                result += "J";
            else if (clazz == Float.TYPE)
                result += "F";
            else if (clazz == Double.TYPE)
                result += "D";
            else
                throw new Error("Unrecognized primitive type.");
        } else {
            String className = clazz.getCanonicalName();
            className = className.replace('.', '/');
            result += 'L' + className + ';';
        }

        return result;
    }

    static String getInternalSignature(Executable executable) {
        StringBuilder result = new StringBuilder();
        result.append('(');

        Class[] parameterTypes = executable.getParameterTypes();
        for (Class parameterType : parameterTypes)
            result.append(getInternalTypeName(parameterType));

        result.append(')');
        return result.toString();
    }

    public static Set<String> getReferencedPackageNames(XType xType) {
        Set<String> result = new HashSet<>();
        getReferencedPackageNames(xType, result, new HashSet<>());
        return result;
    }

    private static void getReferencedPackageNames(XType forType, Set<String> packageNames, Set<XType> seenTypes) {
        if (seenTypes.contains(forType))
            return;
        seenTypes.add(forType);

        if (forType instanceof XClass) {
            XClass clazz = (XClass) forType;

            if (clazz.isVoid() || clazz.isPrimitive())
                return;

            if (clazz.isArray()) {
                getReferencedPackageNames(clazz.getArrayComponentType(), packageNames, seenTypes);
            } else {
                packageNames.add(clazz.getPackageName());
            }
        } else if (forType instanceof XParameterizedType) {
            XParameterizedType parameterizedType = (XParameterizedType) forType;
            getReferencedPackageNames(parameterizedType.getRawType(), packageNames, seenTypes);

            for (XType item : parameterizedType.getActualTypeArguments())
                getReferencedPackageNames(item, packageNames, seenTypes);
        } else if (forType instanceof XTypeVariable) {
            XType resolvedType = ((XTypeVariable) forType).getResolvedType();

            if (!(resolvedType instanceof XTypeVariable)) {
                getReferencedPackageNames(resolvedType, packageNames, seenTypes);
                return;
            }

            XTypeVariable typeVariable = (XTypeVariable) resolvedType;

            for (XType bound : typeVariable.getBounds())
                getReferencedPackageNames(bound, packageNames, seenTypes);
        } else if (forType instanceof XWildcardType) {
        } else if (forType instanceof XGenericArrayType) {
            XGenericArrayType genericArrayType = (XGenericArrayType) forType;
            getReferencedPackageNames(genericArrayType.getGenericComponentType(), packageNames, seenTypes);
        }
    }

    static boolean getMethodsAreEquivalent(Method method1, Method method2) {
        if ((!Objects.equals(method1.getName(), method2.getName())))
            return false;

        if (!method1.getReturnType().equals(method2.getReturnType()))
            return false;

        return equalParamTypes(method1.getParameterTypes(), method2.getParameterTypes());
    }

    private static boolean equalParamTypes(Class[] params1, Class[] params2) {
        if (params1.length == params2.length) {
            for (int i = 0; i < params1.length; i++) {
                if (params1[i] != params2[i])
                    return false;
            }
            return true;
        }
        return false;
    }
}

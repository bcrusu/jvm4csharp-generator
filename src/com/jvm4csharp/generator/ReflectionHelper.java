package com.jvm4csharp.generator;

import java.lang.reflect.*;
import java.util.*;
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

    public static boolean isAbstract(int modifier) {
        return Modifier.isAbstract(modifier);
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

    public static boolean isAbstract(Member member) {
        return isAbstract(member.getModifiers());
    }

    public static boolean isAbstract(Class clazz) {
        return Modifier.isAbstract(clazz.getModifiers());
    }

    public static List<Field> getPublicDeclaredFields(Class clazz) {
        return Arrays.asList(clazz.getDeclaredFields())
                .stream()
                .filter(x -> isPublic(x))
                .collect(Collectors.toList());
    }

    public static List<Method> getPublicDeclaredMethods(Class clazz) {
        List<Method> declaredMethods = Arrays.asList(clazz.getDeclaredMethods())
                .stream()
                .filter(x -> isPublic(x))
                .collect(Collectors.toList());

        if (clazz.isInterface()) {
            return declaredMethods;
        }

        List<Method> result = new ArrayList<>();

        // filter superclass methods with the same signature and return type
        if (clazz != Object.class) {
            Class superclass = clazz.getSuperclass();
            List<Method> superclassMethods = Arrays.asList(superclass.getMethods());

            for (Method method : declaredMethods) {
                boolean ok = true;
                for (Method superclassMethod : superclassMethods)
                    if (getMethodsAreEquivalent(method, superclassMethod)) {
                        ok = false;
                        break;
                    }

                if (ok)
                    result.add(method);
            }
        } else {
            result.addAll(declaredMethods);
        }

        // include missing methods from implemented interfaces
        if (isAbstract(clazz)) {
            if (clazz.getName().contains("Writer"))
            {
                Class[] interface22s = clazz.getInterfaces();
            }

            Class[] interfaces = clazz.getInterfaces();
            for (Class iface : interfaces) {
                Method[] interfaceMethods = iface.getMethods();
                for (Method interfaceMethod : interfaceMethods) {
                    boolean ok = true;
                    for (Method method : result) {
                        if (getMethodsAreEquivalent(method, interfaceMethod)) {
                            ok = false;
                            break;
                        }
                    }

                    if (ok)
                        result.add(interfaceMethod);
                }
            }
        }

        return result;
    }

    public static List<Constructor> getPublicDeclaredConstructors(Class clazz) {
        return Arrays.asList(clazz.getDeclaredConstructors())
                .stream()
                .filter(x -> isPublic(x))
                .collect(Collectors.toList());
    }

    public static List<Type> getImplementedInterfaces(Class clazz) {
        return Arrays.asList(clazz.getGenericInterfaces())
                .stream()
                .collect(Collectors.toList());
    }

    public static String getInternalTypeName(Class clazz) {
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

    public static String getInternalSignature(Executable executable) {
        StringBuilder result = new StringBuilder();
        result.append('(');

        Class[] parameterTypes = executable.getParameterTypes();
        for (Class parameterType : parameterTypes)
            result.append(getInternalTypeName(parameterType));

        result.append(')');
        return result.toString();
    }

    public static String getInternalSignature(Method method) {
        StringBuilder result = new StringBuilder();
        result.append(getInternalSignature((Executable) method));
        result.append(getInternalTypeName(method.getReturnType()));
        return result.toString();
    }

    public static String getInternalSignature(Constructor constructor) {
        StringBuilder result = new StringBuilder();
        result.append(getInternalSignature((Executable) constructor));
        result.append(getInternalTypeName(Void.TYPE));
        return result.toString();
    }

    public static boolean getMethodsAreEquivalent(Method method1, Method method2) {
        if ((method1.getName() != method2.getName()))
            return false;

        //TODO: C# requires 'new' keyword
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

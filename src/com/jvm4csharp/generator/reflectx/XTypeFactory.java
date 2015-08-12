package com.jvm4csharp.generator.reflectx;

import java.lang.reflect.*;

public class XTypeFactory {
    public static XClassDefinition createClassDefinition(Type type) {
        if (type instanceof Class) {
            Class clazz = (Class) type;
            if (!canCreateClassDefinition(clazz))
                throw new IllegalArgumentException(String.format("Cannot create class definition from type '%1s'", type));

            return new XClassDefinition(clazz);
        }
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            Type rawType = parameterizedType.getRawType();

            if (!(rawType instanceof Class) || !canCreateClassDefinition((Class) rawType))
                throw new IllegalArgumentException(String.format("Cannot create class definition from type '%1s'", type));

            return new XClassDefinition(parameterizedType);
        }

        throw new IllegalArgumentException(String.format("Unsupported type '%1s'.", type));
    }

    static XType createType(Type type) {
        if (type instanceof Class) {
            Class clazz = (Class) type;
            return new XClass(clazz);
        }
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            return new XParameterizedType(parameterizedType);
        }
        if (type instanceof TypeVariable) {
            TypeVariable typeVariable = (TypeVariable) type;
            return new XTypeVariable(typeVariable);
        }
        if (type instanceof WildcardType) {
            WildcardType wildcardType = (WildcardType) type;
            return new XWildcardType(wildcardType);
        }
        if (type instanceof GenericArrayType) {
            GenericArrayType genericArrayType = (GenericArrayType) type;
            return new XGenericArrayType(genericArrayType);
        }

        throw new IllegalArgumentException(String.format("Unrecognized type '%1s'.", type));
    }

    static XEditableType createEditableType(Type type) {
        XType xType = createType(type);
        return new XEditableType(xType);
    }

    private static boolean canCreateClassDefinition(Class clazz) {
        return !clazz.isPrimitive() && !clazz.isArray();
    }
}

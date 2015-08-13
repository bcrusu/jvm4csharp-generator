package com.jvm4csharp.generator.reflectx;

import java.lang.reflect.*;

public class XClassDefinitionFactory {
    public static XClassDefinition createClassDefinition(XTypeFactory typeFactory, Type type) {
        if (type instanceof Class) {
            Class clazz = (Class) type;
            if (!canCreateClassDefinition(clazz))
                throw new IllegalArgumentException(String.format("Cannot create class definition from type '%1s'", type));

            return new XClassDefinition(typeFactory, clazz);
        }
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            Type rawType = parameterizedType.getRawType();

            if (!(rawType instanceof Class) || !canCreateClassDefinition((Class) rawType))
                throw new IllegalArgumentException(String.format("Cannot create class definition from type '%1s'", type));

            return new XClassDefinition(typeFactory, parameterizedType);
        }

        throw new IllegalArgumentException(String.format("Unsupported type '%1s'.", type));
    }

    public static XClassDefinition createClassDefinition(Type type) {
        return createClassDefinition(new XTypeFactory(), type);
    }

    private static boolean canCreateClassDefinition(Class clazz) {
        return !clazz.isPrimitive() && !clazz.isArray();
    }
}

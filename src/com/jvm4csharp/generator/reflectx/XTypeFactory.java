package com.jvm4csharp.generator.reflectx;

import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;

class XTypeFactory {
    private final Map<Type, XType> _typeCache;

    public XTypeFactory() {
        _typeCache = new HashMap<>();
    }

    public XType getType(Type type) {
        if (type instanceof Class) {
            Class clazz = (Class) type;
            return XClassFactory.getClass(clazz);
        }

        XType result;
        if (!_typeCache.containsKey(type)) {
            result = getTypeInternal(type);
            _typeCache.put(type, result);
        } else
            result = _typeCache.get(type);

        return result;
    }

    // rename the colliding type variable by suffixing with "_suffix[counter]"
    public void renameCollidingTypeVariablesForMembers(Class clazz) {
        Set<String> classTypeParameterNames = Arrays.asList(clazz.getTypeParameters())
                .stream()
                .map(TypeVariable::getName)
                .collect(Collectors.toSet());

        for (Map.Entry<Type, XType> entry : _typeCache.entrySet()) {
            Type key = entry.getKey();
            if (!(key instanceof TypeVariable))
                continue;

            TypeVariable typeVariable = (TypeVariable) key;
            GenericDeclaration genericDeclaration = typeVariable.getGenericDeclaration();

            if (genericDeclaration instanceof Class)
                continue;

            if (getDeclaringClassForMember(genericDeclaration) != clazz)
                continue;

            if (!classTypeParameterNames.contains(typeVariable.getName()))
                continue;

            String suffix = getCollidingTypeVariableSuffix(genericDeclaration);
            String newName = getNewTypeVariableName(typeVariable.getName(), suffix);

            XTypeVariable xTypeVariable = (XTypeVariable) entry.getValue();
            xTypeVariable.setName(newName);
        }
    }

    public void replaceActualTypeArguments(ParameterizedType parameterizedType) {
        Class clazz = (Class) parameterizedType.getRawType();

        TypeVariable[] typeParameters = clazz.getTypeParameters();
        Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();

        Map<String, XType> actualTypeArgumentsMap = new HashMap<>();
        for (int i = 0; i < typeParameters.length; i++) {
            Type actualTypeArgument = actualTypeArguments[i];
            TypeVariable typeParameter = typeParameters[i];
            actualTypeArgumentsMap.put(typeParameter.getName(), getType(actualTypeArgument));
        }

        for (Map.Entry<Type, XType> entry : _typeCache.entrySet()) {
            Type key = entry.getKey();
            if (!(key instanceof TypeVariable))
                continue;

            TypeVariable typeVariable = (TypeVariable) key;
            if (typeVariable.getGenericDeclaration() != clazz)
                continue;

            String typeParameterName = typeVariable.getName();
            if (!actualTypeArgumentsMap.containsKey(typeParameterName))
                continue;

            XType actualTypeArgument = actualTypeArgumentsMap.get(typeParameterName);

            XTypeVariable xTypeVariable = (XTypeVariable) entry.getValue();
            xTypeVariable.setResolvedType(actualTypeArgument);
        }
    }

    private XType getTypeInternal(Type type) {
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            return new XParameterizedType(this, parameterizedType);
        }
        if (type instanceof TypeVariable) {
            TypeVariable typeVariable = (TypeVariable) type;
            return new XTypeVariable(this, typeVariable);
        }
        if (type instanceof WildcardType) {
            WildcardType wildcardType = (WildcardType) type;
            return new XWildcardType(this, wildcardType);
        }
        if (type instanceof GenericArrayType) {
            GenericArrayType genericArrayType = (GenericArrayType) type;
            return new XGenericArrayType(this, genericArrayType);
        }

        throw new IllegalArgumentException(String.format("Unrecognized type '%1s'.", type));
    }

    private static String getCollidingTypeVariableSuffix(GenericDeclaration genericDeclaration) {
        if (genericDeclaration instanceof Method)
            return ((Method) genericDeclaration).getName();

        if (genericDeclaration instanceof Constructor)
            return ((Constructor) genericDeclaration).getDeclaringClass().getSimpleName();

        throw new UnsupportedOperationException("Cannot resolve suffix for type variable generic declaration.");
    }

    private static String getNewTypeVariableName(String variableName, String suffix) {
        if (!variableName.contains("_" + suffix))
            return variableName + "_" + suffix;

        String[] splits = variableName.split(suffix);
        if (splits.length == 1)
            return variableName + "_" + suffix;
        else if (splits.length == 2) {
            int counter = Integer.parseInt(splits[1]);
            counter++;
            return variableName + "_" + suffix + Integer.toString(counter);
        }

        throw new UnsupportedOperationException("Unhandled case");
    }

    private static Class getDeclaringClassForMember(GenericDeclaration member) {
        if (member instanceof Executable)
            return ((Executable) member).getDeclaringClass();

        throw new UnsupportedOperationException("Cannot resolve declaring class for member.");
    }
}

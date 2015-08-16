package com.jvm4csharp.generator.reflectx;

import java.lang.reflect.Constructor;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Method;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class XTypeVariable extends XType {
    private final XTypeFactory _typeFactory;
    private final TypeVariable _typeVariable;
    private String _name;
    private XType _resolvedType;

    XTypeVariable(XTypeFactory typeFactory, TypeVariable typeVariable) {
        _typeFactory = typeFactory;
        _typeVariable = typeVariable;
        _name = typeVariable.getName();
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof XTypeVariable))
            return false;

        XType thisResolvedType = getResolvedType();
        XType otherResolvedType = ((XTypeVariable) other).getResolvedType();

        if (thisResolvedType instanceof XTypeVariable && otherResolvedType instanceof XTypeVariable) {
            XTypeVariable thisResolvedVariable = (XTypeVariable) thisResolvedType;
            XTypeVariable otherResolvedVariable = (XTypeVariable) otherResolvedType;

            if (!thisResolvedVariable._name.equals(otherResolvedVariable._name))
                return false;

            return thisResolvedVariable._typeVariable.getGenericDeclaration().equals(otherResolvedVariable._typeVariable.getGenericDeclaration());
        } else
            return Objects.equals(thisResolvedType, otherResolvedType);
    }

    @Override
    protected String getDisplayName() {
        String displayName = _name + "(" + getGenericDeclarationDisplayName(_typeVariable.getGenericDeclaration()) + ")";

        if (_resolvedType != null)
            return displayName + "->" + _resolvedType.getDisplayName();

        return displayName;
    }

    public String getName() {
        return _name;
    }

    void setName(String newName) {
        if (_resolvedType != null)
            throw new UnsupportedOperationException();

        _name = newName;
    }

    void setResolvedType(XType type) {
        _resolvedType = type;
    }

    public XType getResolvedType() {
        if (_resolvedType == null)
            return this;

        if (!(_resolvedType instanceof XTypeVariable))
            return _resolvedType;

        XTypeVariable typeVariable = (XTypeVariable) _resolvedType;
        return typeVariable.getResolvedType();
    }

    public List<XType> getBounds() {
        return Arrays.asList(_typeVariable.getBounds())
                .stream()
                .map(_typeFactory::getType)
                .collect(Collectors.toList());
    }

    private static String getGenericDeclarationDisplayName(GenericDeclaration genericDeclaration) {
        if (genericDeclaration instanceof Class)
            return "class " + ((Class) genericDeclaration).getSimpleName();

        if (genericDeclaration instanceof Method)
            return "method " + ((Method) genericDeclaration).getName();

        if (genericDeclaration instanceof Constructor)
            return "constructor " + ((Constructor) genericDeclaration).getDeclaringClass().getSimpleName();

        throw new UnsupportedOperationException("Cannot resolve suffix for type variable generic declaration.");
    }
}

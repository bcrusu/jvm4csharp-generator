package com.jvm4csharp.generator.reflectx;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.GenericDeclaration;
import java.util.HashSet;
import java.util.Set;

public class XGenericArrayType extends XType {
    private final GenericArrayType _genericArrayType;
    private XEditableType _getGenericComponentType;

    XGenericArrayType(GenericArrayType genericArrayType) {
        _genericArrayType = genericArrayType;
        _getGenericComponentType = XTypeFactory.createEditableType(genericArrayType.getGenericComponentType());
    }

    @Override
    void replaceTypeVariable(GenericDeclaration genericDeclaration, String variableName, XType newType) {
        _getGenericComponentType.replaceTypeVariable(genericDeclaration, variableName, newType);
    }

    @Override
    public Set<String> getReferencedPackageNames() {
        HashSet<String> result = new HashSet<>();
        result.addAll(getGenericComponentType().getReferencedPackageNames());
        return result;
    }

    @Override
    public XTypeCompareResult compareTo(XType other) {
        if (!(other instanceof XGenericArrayType))
            return XTypeCompareResult.NotEqual;

        XGenericArrayType other2 = (XGenericArrayType) other;
        if (_genericArrayType.equals(other2._genericArrayType))
            return XTypeCompareResult.Equal;

        return getGenericComponentType().compareTo(other2.getGenericComponentType());
    }

    @Override
    protected String getDisplayName() {
        return _getGenericComponentType.getType().getDisplayName() + "[]";
    }

    public XType getGenericComponentType() {
        return _getGenericComponentType.getType();
    }
}

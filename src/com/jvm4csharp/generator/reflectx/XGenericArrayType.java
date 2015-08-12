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

    private XGenericArrayType(XGenericArrayType toClone) {
        _genericArrayType = toClone._genericArrayType;
        _getGenericComponentType = toClone._getGenericComponentType.clone();
    }

    @Override
    void replaceTypeVariable(GenericDeclaration variableOwner, String variableName, XType newType) {
        _getGenericComponentType.replaceTypeVariable(variableOwner, variableName, newType);
    }

    @Override
    void renameTypeVariable(GenericDeclaration variableOwner, String oldName, String newName) {
        _getGenericComponentType.renameTypeVariable(variableOwner, oldName, newName);
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
    public XType clone() {
        return new XGenericArrayType(this);
    }

    @Override
    protected String getDisplayName() {
        return _getGenericComponentType.getType().getDisplayName() + "[]";
    }

    public XType getGenericComponentType() {
        return _getGenericComponentType.getType();
    }
}

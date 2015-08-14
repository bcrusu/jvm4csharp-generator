package com.jvm4csharp.generator.reflectx;

import java.lang.reflect.GenericArrayType;

public class XGenericArrayType extends XType {
    private final XTypeFactory _typeFactory;
    private final GenericArrayType _genericArrayType;
    private final XType _getGenericComponentType;

    XGenericArrayType(XTypeFactory typeFactory, GenericArrayType genericArrayType) {
        _typeFactory = typeFactory;
        _genericArrayType = genericArrayType;
        _getGenericComponentType = typeFactory.getType(genericArrayType.getGenericComponentType());
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
        return _getGenericComponentType.getDisplayName() + "[]";
    }

    public XType getGenericComponentType() {
        return _getGenericComponentType;
    }
}

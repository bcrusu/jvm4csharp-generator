package com.jvm4csharp.generator.reflectx;

import java.lang.reflect.GenericArrayType;
import java.util.Objects;

public class XGenericArrayType extends XType {
    private final XTypeFactory _typeFactory;
    private final GenericArrayType _genericArrayType;
    private final XType _genericComponentType;

    XGenericArrayType(XTypeFactory typeFactory, GenericArrayType genericArrayType) {
        _typeFactory = typeFactory;
        _genericArrayType = genericArrayType;
        _genericComponentType = typeFactory.getType(genericArrayType.getGenericComponentType());
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof XGenericArrayType))
            return false;

        XGenericArrayType other2 = (XGenericArrayType) other;
        return Objects.equals(_genericComponentType, other2._genericComponentType);
    }

    @Override
    protected String getDisplayName() {
        return _genericComponentType.getDisplayName() + "[]";
    }

    public XType getGenericComponentType() {
        return _genericComponentType;
    }
}

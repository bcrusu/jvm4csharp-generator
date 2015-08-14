package com.jvm4csharp.generator.reflectx;

import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class XTypeVariable extends XType {
    private final XTypeFactory typeFactory;
    private final TypeVariable _typeVariable;
    private String _name;
    private XType _resolvedType;

    XTypeVariable(XTypeFactory typeFactory, TypeVariable typeVariable) {
        this.typeFactory = typeFactory;
        _typeVariable = typeVariable;
        _name = typeVariable.getName();
    }

    @Override
    public XTypeCompareResult compareTo(XType other) {
        if (!(other instanceof XTypeVariable))
            return XTypeCompareResult.NotEqual;

        XTypeVariable other2 = (XTypeVariable) other;
        if (!_name.equals(other2._name))
            return XTypeCompareResult.NotEqual;

        if (!Objects.equals(_resolvedType, other2._resolvedType))
            return XTypeCompareResult.NotEqual;

        return XTypeCompareResult.Equal;
    }

    @Override
    protected String getDisplayName() {
        if (_resolvedType != null)
            return _resolvedType.getDisplayName();

        return _name;
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
                .map(typeFactory::getType)
                .collect(Collectors.toList());
    }
}

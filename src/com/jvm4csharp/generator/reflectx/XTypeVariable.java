package com.jvm4csharp.generator.reflectx;

import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class XTypeVariable extends XType {
    private final XTypeFactory typeFactory;
    private final TypeVariable _typeVariable;
    private String _name;
    private IGenericDeclaration _genericDeclaration;
    private XType _resolvedType;

    XTypeVariable(XTypeFactory typeFactory, TypeVariable typeVariable) {
        this.typeFactory = typeFactory;
        _typeVariable = typeVariable;
        _name = typeVariable.getName();
    }

    @Override
    public Set<String> getReferencedPackageNames() {
        return new HashSet<>();
    }

    @Override
    public XTypeCompareResult compareTo(XType other) {
        if (!(other instanceof XTypeVariable))
            return XTypeCompareResult.NotEqual;

        XTypeVariable other2 = (XTypeVariable) other;
        if (!_name.equals(other2._name))
            return XTypeCompareResult.NotEqual;

        if (_resolvedType == null && other2._resolvedType != null)
            return XTypeCompareResult.NotEqual;

        if (_resolvedType == null)
            return XTypeCompareResult.Equal;
        
        if (!_resolvedType.equals(other2._resolvedType))
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
        _name = newName;
    }

    void setResolvedType(XType type) {
        _resolvedType = type;
    }

    XType getResolvedType() {
        if (_resolvedType != null)
            return _resolvedType;

        return this;
    }

    public List<XType> getBounds() {
        return Arrays.asList(_typeVariable.getBounds())
                .stream()
                .map(typeFactory::getType)
                .collect(Collectors.toList());
    }
}

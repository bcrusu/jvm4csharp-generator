package com.jvm4csharp.generator.reflectx;

import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class XTypeVariable extends XType {
    private final TypeVariable _typeVariable;
    private final List<XEditableType> _bounds;
    private String _name;

    XTypeVariable(TypeVariable typeVariable) {
        _typeVariable = typeVariable;
        _name = typeVariable.getName();
        _bounds = Arrays.asList(typeVariable.getBounds())
                .stream()
                .map(x -> XTypeFactory.createEditableType(x))
                .collect(Collectors.toList());
    }

    private XTypeVariable(XTypeVariable toClone) {
        _typeVariable = toClone._typeVariable;
        _name = toClone._name;
        _bounds = toClone._bounds
                .stream()
                .map(x -> x.clone())
                .collect(Collectors.toList());
    }

    @Override
    void replaceTypeVariable(GenericDeclaration variableOwner, String variableName, XType newType) {
        for (XEditableType item : _bounds)
            item.replaceTypeVariable(variableOwner, variableName, newType);
    }

    @Override
    void renameTypeVariable(GenericDeclaration variableOwner, String oldName, String newName) {
        if (_name.equals(newName))
            return;

        _name = newName;
        for (XEditableType item : _bounds)
            item.renameTypeVariable(variableOwner, oldName, newName);
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
        if (_name.equals(other2._name))
            return XTypeCompareResult.Equal;

        return XTypeCompareResult.NotEqual;
    }

    @Override
    public XType clone() {
        return new XTypeVariable(this);
    }

    @Override
    protected String getDisplayName() {
        return _name;
    }

    public String getName() {
        return _name;
    }

    public List<XType> getBounds() {
        return _bounds.stream()
                .map(x -> x.getType())
                .collect(Collectors.toList());
    }

    public GenericDeclaration getGenericDeclaration() {
        return _typeVariable.getGenericDeclaration();
    }

    void setName(String newName) {
        renameTypeVariable(_typeVariable.getGenericDeclaration(), _name, newName);
    }
}

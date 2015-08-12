package com.jvm4csharp.generator.reflectx;

import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.TypeVariable;
import java.util.HashSet;
import java.util.Set;

public class XTypeVariable extends XType {
    private final TypeVariable _typeVariable;
    private String _name;

    XTypeVariable(TypeVariable typeVariable) {
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
        if (_name.equals(other2._name))
            return XTypeCompareResult.Equal;

        return XTypeCompareResult.NotEqual;
    }

    @Override
    protected String getDisplayName() {
        return _name;
    }

    public String getName() {
        return _name;
    }

    public GenericDeclaration getGenericDeclaration() {
        return _typeVariable.getGenericDeclaration();
    }

    void setName(String name) {
        _name = name;
    }
}

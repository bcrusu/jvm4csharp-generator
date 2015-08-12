package com.jvm4csharp.generator.reflectx;

import java.lang.reflect.GenericDeclaration;
import java.util.Set;

public abstract class XType {
    // Used when expanding private superclass/interfaces
    void replaceTypeVariable(GenericDeclaration genericDeclaration, String variableName, XType newType){
    }

    public abstract Set<String> getReferencedPackageNames();

    public abstract XTypeCompareResult compareTo(XType other);

    // Used for debugging
    protected abstract String getDisplayName();

    @Override
    public String toString() {
        return getDisplayName();
    }
}

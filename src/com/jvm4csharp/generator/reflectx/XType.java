package com.jvm4csharp.generator.reflectx;

import java.lang.reflect.GenericDeclaration;
import java.util.Set;

public abstract class XType {
    // Used when expanding private superclass/interfaces
    void replaceTypeVariable(GenericDeclaration variableOwner, String variableName, XType newType){
    }

    void renameTypeVariable(GenericDeclaration variableOwner, String oldName, String newName) {
    }

    public abstract Set<String> getReferencedPackageNames();

    public abstract XTypeCompareResult compareTo(XType other);

    public abstract XType clone();

    // Used for debugging
    protected abstract String getDisplayName();

    @Override
    public String toString() {
        return getDisplayName();
    }
}

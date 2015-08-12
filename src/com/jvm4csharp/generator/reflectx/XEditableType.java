package com.jvm4csharp.generator.reflectx;

import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.TypeVariable;

class XEditableType {
    private XType _xType;

    XEditableType(XType xType) {
        _xType = xType;
    }

    boolean replaceTypeVariable(GenericDeclaration genericDeclaration, String variableName, XType newType) {
        if (shouldReplaceTypeVariable(genericDeclaration, variableName)) {
            _xType = newType;
            return true;
        } else {
            _xType.replaceTypeVariable(genericDeclaration, variableName, newType);
            return false;
        }
    }

    void renameCollidingTypeVariables(GenericDeclaration genericDeclaration, String ownerName) {
        if (!(_xType instanceof XTypeVariable))
            return;

        TypeVariable[] typeParameters = genericDeclaration.getTypeParameters();
        if (typeParameters.length == 0)
            return;

        XTypeVariable variable = (XTypeVariable) _xType;
        String variableName = variable.getName();
        GenericDeclaration variableGenericDeclaration = variable.getGenericDeclaration();

        for (TypeVariable typeParameter : typeParameters)
            if (variableGenericDeclaration != genericDeclaration && variableName.equals(typeParameter.getName())) {
                if (!variableName.contains("_" + ownerName)) {
                    variable.setName(variableName + "_" + ownerName);
                } else {
                    String[] splits = variableName.split(ownerName);
                    if (splits.length == 1)
                        variable.setName(variableName + "_" + ownerName);
                    else if (splits.length == 2) {
                        int counter = Integer.parseInt(splits[1]);
                        counter++;
                        variable.setName(variableName + "_" + ownerName + Integer.toString(counter));
                    } else
                        throw new UnsupportedOperationException();
                }

                break;  // type parameters have unique names
            }
    }

    public XType getType() {
        return _xType;
    }

    @Override
    public String toString() {
        return getType().toString();
    }

    private boolean shouldReplaceTypeVariable(GenericDeclaration genericDeclaration, String variableName) {
        if (!(_xType instanceof XTypeVariable))
            return false;

        XTypeVariable xTypeVariable = (XTypeVariable) _xType;
        return xTypeVariable.getGenericDeclaration() == genericDeclaration && variableName.equals(xTypeVariable.getName());
    }
}

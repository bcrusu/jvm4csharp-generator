package com.jvm4csharp.generator.reflectx;

import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.TypeVariable;

class XEditableType {
    private XType _xType;

    XEditableType(XType xType) {
        _xType = xType;
    }

    private XEditableType(XEditableType toClone) {
        _xType = toClone._xType.clone();
    }

    boolean replaceTypeVariable(GenericDeclaration variableOwner, String variableName, XType newType) {
        if (typeVariableMatches(variableOwner, variableName)) {
            _xType = newType.clone();
            return true;
        } else {
            _xType.replaceTypeVariable(variableOwner, variableName, newType);
            return false;
        }
    }

    boolean renameTypeVariable(GenericDeclaration variableOwner, String oldName, String newName) {
        if (typeVariableMatches(variableOwner, oldName)) {
            XTypeVariable xTypeVariable = (XTypeVariable) _xType;
            xTypeVariable.setName(newName);
            return true;
        } else {
            _xType.renameTypeVariable(variableOwner, oldName, newName);
            return false;
        }
    }

    void renameCollidingTypeVariables(GenericDeclaration genericDeclaration, String suffix) {
        if (!(_xType instanceof XTypeVariable))
            return;

        TypeVariable[] typeParameters = genericDeclaration.getTypeParameters();
        if (typeParameters.length == 0)
            return;

        XTypeVariable variable = (XTypeVariable) _xType;
        String variableName = variable.getName();
        GenericDeclaration variableGenericDeclaration = variable.getGenericDeclaration();

        // rename the colliding type variable by suffixing with "_ownerName[counter]"
        for (TypeVariable typeParameter : typeParameters)
            if (variableGenericDeclaration != genericDeclaration && variableName.equals(typeParameter.getName())) {
                if (!variableName.contains("_" + suffix)) {
                    variable.setName(variableName + "_" + suffix);
                } else {
                    String[] splits = variableName.split(suffix);
                    if (splits.length == 1)
                        variable.setName(variableName + "_" + suffix);
                    else if (splits.length == 2) {
                        int counter = Integer.parseInt(splits[1]);
                        counter++;
                        variable.setName(variableName + "_" + suffix + Integer.toString(counter));
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

    private boolean typeVariableMatches(GenericDeclaration variableOwner, String variableName) {
        if (!(_xType instanceof XTypeVariable))
            return false;

        XTypeVariable xTypeVariable = (XTypeVariable) _xType;
        return xTypeVariable.getGenericDeclaration() == variableOwner && variableName.equals(xTypeVariable.getName());
    }

    public XEditableType clone() {
        return new XEditableType(this);
    }
}

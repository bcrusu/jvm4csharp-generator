package com.jvm4csharp.generator.reflectx;

import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

public class XMethod extends XExecutable {
    private final Method _method;
    private XEditableType _returnType;

    XMethod(XClass declaringClass, Method method) {
        super(declaringClass, method);
        _method = method;
    }

    void replaceTypeVariable(GenericDeclaration genericDeclaration, String variableName, XType newType) {
        if (isStatic())
            return;

        getEditableReturnType().replaceTypeVariable(genericDeclaration, variableName, newType);

        for (XEditableType type : getEditableParameterTypes())
            type.replaceTypeVariable(genericDeclaration, variableName, newType);
    }

    void renameCollidingTypeVariables(GenericDeclaration genericDeclaration) {
        super.renameCollidingTypeVariables(genericDeclaration);
        String methodName = getName();
        getEditableReturnType().renameCollidingTypeVariables(genericDeclaration, methodName);
    }

    @Override
    public Set<String> getReferencedPackageNames() {
        Set<String> result = super.getReferencedPackageNames();
        result.addAll(getReturnType().getReferencedPackageNames());
        return result;
    }

    public String getName() {
        return _method.getName();
    }

    public boolean isStatic(){
        return XUtils.isStatic(_method);
    }

    public boolean isFinal(){
        return XUtils.isFinal(_method);
    }

    public boolean isDefault(){
        return _method.isDefault();
    }

    public boolean isAbstract(){
        return XUtils.isAbstract(_method);
    }

    public XType getReturnType() {
        return getEditableReturnType().getType();
    }

    public boolean isVoidReturnType(){
        return _method.getReturnType() == Void.TYPE;
    }

    @Override
    public String getInternalSignature() {
        StringBuilder result = new StringBuilder();
        result.append(XUtils.getInternalSignature(_method));
        result.append(XUtils.getInternalTypeName(_method.getReturnType()));
        return result.toString();
    }

    private XEditableType getEditableReturnType() {
        if (_returnType == null)
            _returnType = XTypeFactory.createEditableType(_method.getGenericReturnType());
        return _returnType;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getReturnType());
        sb.append(" ");
        sb.append(getName());
        sb.append("(");

        List<XType> parameterTypes = getParameterTypes();
        for (int i = 0; i < parameterTypes.size(); i++) {
            sb.append(parameterTypes.get(i));

            if (i < parameterTypes.size() - 1)
                sb.append(", ");
        }

        sb.append(")");
        return sb.toString();
    }

    public XMethodCompareResult compareTo(XMethod other) {
        if (!getName().equals(other.getName()))
            return XMethodCompareResult.NotEqual;

        List<XType> thisParameterTypes = getParameterTypes();
        List<XType> otherParameterTypes = other.getParameterTypes();

        if (thisParameterTypes.size() != otherParameterTypes.size())
            return XMethodCompareResult.NotEqual;

        for (int i = 0; i < thisParameterTypes.size(); i++)
            if (thisParameterTypes.get(i).compareTo(otherParameterTypes.get(i)) != XTypeCompareResult.Equal)
                return XMethodCompareResult.NotEqual;

        XType thisReturnType = getReturnType();
        XType otherReturnType = other.getReturnType();
        XTypeCompareResult returnTypeCompareResult = thisReturnType.compareTo(otherReturnType);

        if (returnTypeCompareResult == XTypeCompareResult.Equal)
            return XMethodCompareResult.SameSignature_SameReturnType;

        if (returnTypeCompareResult == XTypeCompareResult.MoreSpecific)
            return XMethodCompareResult.SameSignature_MoreSpecificReturnType;

        return XMethodCompareResult.SameSignature_DifferentReturnType;
    }
}

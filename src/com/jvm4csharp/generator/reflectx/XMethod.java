package com.jvm4csharp.generator.reflectx;

import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

public class XMethod extends XExecutable {
    private final Method _method;
    private final XEditableType _returnType;

    XMethod(XClassDefinition declaringClass, Method method) {
        super(declaringClass, method);
        _method = method;
        _returnType = XTypeFactory.createEditableType(_method.getGenericReturnType());
    }

    void replaceTypeVariable(GenericDeclaration variableOwner, String variableName, XType newType) {
        if (isStatic()) return;

        _returnType.replaceTypeVariable(variableOwner, variableName, newType);

        for (XEditableType type : getEditableParameterTypes())
            type.replaceTypeVariable(variableOwner, variableName, newType);
    }

    void renameCollidingTypeVariables(GenericDeclaration variableOwner) {
        super.renameCollidingTypeVariables(variableOwner);
        String suffix = getName();
        _returnType.renameCollidingTypeVariables(variableOwner, suffix);
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

    public boolean isStatic() {
        return XUtils.isStatic(_method);
    }

    public boolean isFinal() {
        return XUtils.isFinal(_method);
    }

    public boolean isDefault() {
        return _method.isDefault();
    }

    public boolean isAbstract() {
        return XUtils.isAbstract(_method);
    }

    public XType getReturnType() {
        return _returnType.getType();
    }

    public boolean isVoidReturnType() {
        return _method.getReturnType() == Void.TYPE;
    }

    @Override
    public String getInternalSignature() {
        StringBuilder result = new StringBuilder();
        result.append(XUtils.getInternalSignature(_method));
        result.append(XUtils.getInternalTypeName(_method.getReturnType()));
        return result.toString();
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

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof XMethod))
            return false;

        XMethod other2 = (XMethod) other;
        if (!getName().equals(other2.getName()))
            return false;

        List<XType> thisParameterTypes = getParameterTypes();
        List<XType> otherParameterTypes = other2.getParameterTypes();

        if (thisParameterTypes.size() != otherParameterTypes.size())
            return false;

        for (int i = 0; i < thisParameterTypes.size(); i++)
            if (thisParameterTypes.get(i).compareTo(otherParameterTypes.get(i)) != XTypeCompareResult.Equal)
                return false;

        return true;
    }
}

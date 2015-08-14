package com.jvm4csharp.generator.reflectx;

import java.lang.reflect.Method;
import java.util.Set;

public class XMethod extends XExecutable {
    private final Method _method;
    private final XType _returnType;

    XMethod(XClassDefinition declaringClass, Method method) {
        super(declaringClass, method);
        _method = method;
        _returnType = declaringClass.getTypeFactory().getType(_method.getGenericReturnType());
    }

    @Override
    public Set<String> getReferencedPackages() {
        Set<String> result = super.getReferencedPackages();
        result.addAll(getReturnType().getReferencedPackageNames());
        return result;
    }

    @Override
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
        return _returnType;
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
        sb.append(super.toString());
        return sb.toString();
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof XMethod))
            return false;

        return super.equals(other);
    }
}

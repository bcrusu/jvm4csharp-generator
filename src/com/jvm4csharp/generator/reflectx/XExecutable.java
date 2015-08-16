package com.jvm4csharp.generator.reflectx;

import java.lang.reflect.Executable;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class XExecutable implements IGenericDeclaration {
    private final XClassDefinition _declaringClass;
    private final Executable _executable;
    private final List<XType> _parameterTypes;
    private final List<XTypeVariable> _typeParameters;

    XExecutable(XClassDefinition declaringClass, Executable executable) {
        _declaringClass = declaringClass;
        _executable = executable;
        _typeParameters = Arrays.asList(_executable.getTypeParameters())
                .stream()
                .filter(x -> x.getGenericDeclaration() == executable)
                .map(declaringClass.getTypeFactory()::getType)
                .map(x -> (XTypeVariable) x)
                .collect(Collectors.toList());
        _parameterTypes = Arrays.asList(_executable.getGenericParameterTypes())
                .stream()
                .map(declaringClass.getTypeFactory()::getType)
                .collect(Collectors.toList());
    }

    public Set<String> getReferencedPackages() {
        HashSet<String> result = new HashSet<>();

        for (XType item : getParameterTypes())
            result.addAll(item.getReferencedPackageNames());

        return result;
    }

    public List<XType> getParameterTypes() {
        return _parameterTypes;
    }

    public List<String> getParameterNames() {
        return Arrays.asList(_executable.getParameters())
                .stream()
                .map(Parameter::getName)
                .collect(Collectors.toList());
    }

    @Override
    public List<XTypeVariable> getTypeParameters() {
        return _typeParameters;
    }

    @Override
    public boolean hasTypeParameters() {
        return getTypeParameters().size() > 0;
    }

    public abstract String getInternalSignature();

    public abstract String getName();

    public XClassDefinition getDeclaringClass() {
        return _declaringClass;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof XExecutable))
            return false;

        XExecutable other2 = (XExecutable) other;
        if (!getName().equals(other2.getName()))
            return false;

        List<XType> thisParameterTypes = getParameterTypes();
        List<XType> otherParameterTypes = other2.getParameterTypes();

        if (thisParameterTypes.size() != otherParameterTypes.size())
            return false;

        for (int i = 0; i < thisParameterTypes.size(); i++)
            if (!thisParameterTypes.get(i).equals(otherParameterTypes.get(i)))
                return false;

        return true;
    }

    public boolean isEquivalent(XExecutable other) {
        if (other == null)
            return false;

        if (!getName().equals(other.getName()))
            return false;

        List<XType> thisParameterTypes = getParameterTypes();
        List<XType> otherParameterTypes = other.getParameterTypes();

        if (thisParameterTypes.size() != otherParameterTypes.size())
            return false;

        for (int i = 0; i < thisParameterTypes.size(); i++)
            if (!thisParameterTypes.get(i).isEquivalent(otherParameterTypes.get(i)))
                return false;

        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getName());
        sb.append("(");

        List<XType> parameterTypes = _parameterTypes;
        for (int i = 0; i < parameterTypes.size(); i++) {
            sb.append(parameterTypes.get(i));

            if (i < parameterTypes.size() - 1)
                sb.append(", ");
        }

        sb.append(")");
        return sb.toString();
    }
}

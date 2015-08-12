package com.jvm4csharp.generator.reflectx;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class XExecutable implements XGenericDeclaration {
    private final XClass _declaringClass;
    private final Executable _executable;
    private List<XEditableType> _parameterTypes;
    private List<XEditableType> _typeParameters;

    XExecutable(XClass declaringClass, Executable executable) {
        _declaringClass = declaringClass;
        _executable = executable;
    }

    void renameCollidingTypeVariables(GenericDeclaration genericDeclaration) {
        String ownerName = _executable.getName();
        if (_executable instanceof Constructor)
            ownerName = _declaringClass.getSimpleName();

        for (XEditableType item : getEditableParameterTypes())
            item.renameCollidingTypeVariables(genericDeclaration, ownerName);

        for (XEditableType item : getEditableTypeParameters())
            item.renameCollidingTypeVariables(genericDeclaration, ownerName);
    }

    public Set<String> getReferencedPackageNames() {
        HashSet<String> result = new HashSet<>();

        for (XType item : getParameterTypes())
            result.addAll(item.getReferencedPackageNames());

        return result;
    }

    public List<XType> getParameterTypes() {
        return getEditableParameterTypes()
                .stream()
                .map(XEditableType::getType)
                .collect(Collectors.toList());
    }

    public List<String> getParameterNames() {
        return Arrays.asList(_executable.getParameters())
                .stream()
                .map(Parameter::getName)
                .collect(Collectors.toList());
    }

    public String getName(){
        return getDeclaringClass().getSimpleName();
    }

    @Override
    public List<XTypeVariable> getTypeParameters() {
        return getEditableTypeParameters()
                .stream()
                .map(XEditableType::getType)
                .map(x -> (XTypeVariable) x)
                .collect(Collectors.toList());
    }

    public XClass getDeclaringClass() {
        return _declaringClass;
    }

    public abstract String getInternalSignature();

    protected List<XEditableType> getEditableParameterTypes() {
        if (_parameterTypes == null)
            _parameterTypes = Arrays.asList(_executable.getGenericParameterTypes())
                    .stream()
                    .map(XTypeFactory::createEditableType)
                    .collect(Collectors.toList());

        return _parameterTypes;
    }

    protected List<XEditableType> getEditableTypeParameters() {
        if (_typeParameters == null)
            _typeParameters = Arrays.asList(_executable.getTypeParameters())
                    .stream()
                    .map(XTypeFactory::createEditableType)
                    .collect(Collectors.toList());

        return _typeParameters;
    }
}

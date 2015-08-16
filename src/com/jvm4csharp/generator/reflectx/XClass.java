package com.jvm4csharp.generator.reflectx;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class XClass extends XType implements IGenericDeclaration {
    private final XTypeFactory _typeFactory;
    private final Class _class;
    private XClass _arrayComponentType;
    private List<XTypeVariable> _typeParameters;

    XClass(XTypeFactory typeFactory, Class clazz) {
        _typeFactory = typeFactory;
        _class = clazz;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof XClass))
            return false;

        return _class.equals(((XClass) other)._class);
    }

    @Override
    protected String getDisplayName() {
        return _class.getName();
    }

    public String getSimpleName() {
        return _class.getSimpleName();
    }

    public String getPackageName() {
        return _class.getPackage().getName();
    }

    public boolean isVoid() {
        return _class == Void.TYPE;
    }

    public boolean isPrimitive() {
        return _class.isPrimitive();
    }

    public boolean isArray() {
        return _class.isArray();
    }

    public boolean isPublic() {
        return XUtils.isPublic(_class);
    }

    public boolean isStatic() {
        return XUtils.isStatic(_class);
    }

    public boolean isAbstract() {
        return XUtils.isAbstract(_class);
    }

    public boolean isFinal() {
        return XUtils.isFinal(_class);
    }

    public boolean isEnum() {
        return _class.isEnum();
    }

    public boolean isInterface() {
        return _class.isInterface();
    }

    public XClass getArrayComponentType() {
        if (_arrayComponentType == null && isArray())
            _arrayComponentType = XClassFactory.getClass(_class.getComponentType());
        return _arrayComponentType;
    }

    public boolean isClass(Class clazz) {
        return _class == clazz;
    }

    public boolean isAssignableFrom(XClass other) {
        return _class.isAssignableFrom(other._class);
    }

    public XClass getDeclaringClass() {
        Class declaringClass = _class.getDeclaringClass();
        return declaringClass != null ? XClassFactory.getClass(declaringClass) : null;
    }

    public List<XTypeVariable> getTypeParameters() {
        if (_typeParameters == null)
            _typeParameters = Arrays.asList(_class.getTypeParameters())
                    .stream()
                    .map(_typeFactory::getType)
                    .map(x -> (XTypeVariable) x)
                    .collect(Collectors.toList());
        return _typeParameters;
    }

    @Override
    public boolean hasTypeParameters() {
        return getTypeParameters().size() > 0;
    }

    public String getInternalTypeName() {
        return XUtils.getInternalTypeName(_class);
    }
}

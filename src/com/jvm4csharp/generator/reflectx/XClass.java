package com.jvm4csharp.generator.reflectx;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class XClass extends XType {
    private final Class _class;
    private XClass _arrayComponentType;
    private List<XTypeVariable> _typeParameters;

    XClass(Class clazz) {
        _class = clazz;
    }

    @Override
    public Set<String> getReferencedPackageNames() {
        HashSet<String> result = new HashSet<>();
        if (isVoid() || isPrimitive())
            return result;

        if (isArray()) {
            result.addAll(getArrayComponentType().getReferencedPackageNames());
        } else {
            result.add(_class.getPackage().getName());
        }

        return result;
    }

    @Override
    public XTypeCompareResult compareTo(XType other) {
        if (!(other instanceof XClass))
            return XTypeCompareResult.NotEqual;

        XClass other2 = (XClass) other;
        if (_class.equals(other2._class))
            return XTypeCompareResult.Equal;
        if (other2._class.isAssignableFrom(_class))
            return XTypeCompareResult.MoreSpecific;

        return XTypeCompareResult.NotEqual;
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
        if (_arrayComponentType == null && _class.isArray())
            _arrayComponentType = (XClass) XTypeFactory.createType(_class.getComponentType());
        return _arrayComponentType;
    }

    public boolean isClass(Class clazz) {
        return _class == clazz;
    }

    public XClass getDeclaringClass() {
        Class declaringClass = _class.getDeclaringClass();
        return declaringClass != null ? (XClass) XTypeFactory.createType(declaringClass) : null;
    }

    public List<XTypeVariable> getTypeParameters() {
        if (_typeParameters == null)
            _typeParameters = Arrays.asList(_class.getTypeParameters())
                    .stream()
                    .map(XTypeVariable::new)
                    .collect(Collectors.toList());

        return _typeParameters;
    }

    public String getInternalTypeName() {
        return XUtils.getInternalTypeName(_class);
    }
}

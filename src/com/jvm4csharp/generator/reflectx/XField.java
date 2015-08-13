package com.jvm4csharp.generator.reflectx;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

public class XField {
    private final XClassDefinition _declaringClass;
    private final Field _field;

    XField(XClassDefinition declaringClass, Field field) {
        _declaringClass = declaringClass;
        _field = field;
    }

    public Set<String> getReferencedPackageNames() {
        HashSet<String> result = new HashSet<>();
        result.addAll(getType().getReferencedPackageNames());
        return result;
    }

    public String getInternalTypeName() {
        return XUtils.getInternalTypeName(_field.getType());
    }

    public String getName() {
        return _field.getName();
    }

    public boolean isStatic(){
        return XUtils.isStatic(_field);
    }

    public boolean isFinal(){
        return XUtils.isFinal(_field);
    }

    public XClassDefinition getDeclaringClass() {
        return _declaringClass;
    }

    public XType getType() {
        return _declaringClass.getTypeFactory().getType(_field.getGenericType());
    }
}

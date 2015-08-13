package com.jvm4csharp.generator.reflectx;

import java.lang.reflect.Constructor;

public class XConstructor extends XExecutable {
    private final Constructor _constructor;

    XConstructor(XClassDefinition declaringClass, Constructor constructor) {
        super(declaringClass, constructor);
        _constructor = constructor;
    }

    @Override
    public String getInternalSignature() {
        StringBuilder result = new StringBuilder();
        result.append(XUtils.getInternalSignature(_constructor));
        result.append(XUtils.getInternalTypeName(Void.TYPE));
        return result.toString();
    }

    @Override
    public String getName(){
        return getDeclaringClass().getXClass().getSimpleName();
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof XConstructor))
            return false;

        return super.equals(other);
    }
}

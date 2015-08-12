package com.jvm4csharp.generator.reflectx;

import java.lang.reflect.Constructor;

public class XConstructor extends XExecutable {
    private final Constructor _constructor;

    XConstructor(XClass declaringClass, Constructor constructor) {
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
}

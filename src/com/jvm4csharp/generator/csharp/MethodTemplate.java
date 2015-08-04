package com.jvm4csharp.generator.csharp;

import com.jvm4csharp.generator.IItemTemplate;

import java.lang.reflect.Method;

public class MethodTemplate implements IItemTemplate {
    private final Method _method;

    public MethodTemplate(Method method){
        _method = method;
    }
    @Override
    public String[] GetDependencies() {
        return new String[0];
    }

    @Override
    public String GeneratePrivate() {
        return null;
    }

    @Override
    public String GenerateBody() {
        return null;
    }
}

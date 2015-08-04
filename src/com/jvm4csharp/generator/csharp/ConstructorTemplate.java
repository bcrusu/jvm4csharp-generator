package com.jvm4csharp.generator.csharp;

import com.jvm4csharp.generator.IItemTemplate;

import java.lang.reflect.Constructor;

public class ConstructorTemplate implements IItemTemplate {
    private final Constructor _constructor;

    public ConstructorTemplate(Constructor constructor){
        _constructor = constructor;
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

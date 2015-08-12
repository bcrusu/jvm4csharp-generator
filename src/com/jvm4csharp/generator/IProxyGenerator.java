package com.jvm4csharp.generator;

import com.jvm4csharp.generator.reflectx.XClassDefinition;

public interface IProxyGenerator {
    GenerationResult generate(XClassDefinition clazz);
}

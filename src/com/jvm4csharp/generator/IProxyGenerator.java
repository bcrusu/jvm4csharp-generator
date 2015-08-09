package com.jvm4csharp.generator;

public interface IProxyGenerator {
    boolean canGenerate(Class clazz);

    GenerationResult generate(ClassDetails classDetails);
}

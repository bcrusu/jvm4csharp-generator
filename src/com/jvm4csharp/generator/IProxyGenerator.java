package com.jvm4csharp.generator;

public interface IProxyGenerator {
    boolean canGenerate(Class clazz);

    GenerateResult generate(Class clazz);
}

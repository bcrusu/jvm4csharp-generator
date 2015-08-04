package com.jvm4csharp.generator;

public interface IItemTemplate {
    String[] GetDependencies();

    String GeneratePrivate();

    String GenerateBody();
}

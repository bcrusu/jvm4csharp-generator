package com.jvm4csharp.generator.reflectx;

//TODO:
public interface IClassMemberFilter {
    boolean isIncluded(XField field);

    boolean isIncluded(XConstructor constructor);

    boolean isIncluded(XMethod method);
}

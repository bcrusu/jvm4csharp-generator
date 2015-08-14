package com.jvm4csharp.generator.reflectx;

import java.util.List;

public interface IGenericDeclaration{
    List<XTypeVariable> getTypeParameters();

    boolean hasTypeParameters();
}

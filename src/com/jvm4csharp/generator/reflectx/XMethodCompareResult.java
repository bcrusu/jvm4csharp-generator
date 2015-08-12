package com.jvm4csharp.generator.reflectx;

public enum XMethodCompareResult {
    NotEqual,
    SameSignature_SameReturnType,
    SameSignature_MoreSpecificReturnType,
    SameSignature_DifferentReturnType
}

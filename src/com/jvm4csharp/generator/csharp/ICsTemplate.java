package com.jvm4csharp.generator.csharp;

import com.jvm4csharp.generator.GenerateResult;

public interface ICsTemplate {
    GenerateResult generate();

    CsType[] getReferencedCsTypes();
}

package com.jvm4csharp.generator.csharp;

import com.jvm4csharp.generator.IItemTemplate;

public interface ICsItemTemplate extends IItemTemplate{
    CsType[] getReferencedCsTypes();
}

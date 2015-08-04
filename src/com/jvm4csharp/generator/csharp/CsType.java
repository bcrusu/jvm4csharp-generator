package com.jvm4csharp.generator.csharp;

import java.util.HashSet;
import java.util.Set;

public class CsType {
    public CsType(){
        NamespacesUsed = new HashSet<>();
    }
    public String DisplayName;
    public Set<String> NamespacesUsed;
}

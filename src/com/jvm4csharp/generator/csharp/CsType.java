package com.jvm4csharp.generator.csharp;

import java.util.HashSet;
import java.util.Set;

public class CsType {
    public CsType(){
        namespacesUsed = new HashSet<>();
    }
    public String displayName;
    public Set<String> namespacesUsed;
}

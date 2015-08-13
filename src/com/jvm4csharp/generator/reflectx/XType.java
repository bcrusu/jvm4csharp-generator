package com.jvm4csharp.generator.reflectx;

import java.util.Set;

public abstract class XType {
    public abstract Set<String> getReferencedPackageNames();

    public abstract XTypeCompareResult compareTo(XType other);

    // Used for debugging
    protected abstract String getDisplayName();

    @Override
    public String toString() {
        return getDisplayName();
    }
}

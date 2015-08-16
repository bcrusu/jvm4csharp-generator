package com.jvm4csharp.generator.reflectx;

import java.util.Set;

public abstract class XType {
    public Set<String> getReferencedPackageNames() {
        return XUtils.getReferencedPackageNames(this);
    }

    @Override
    public abstract boolean equals(Object other);

    public boolean isEquivalent(XType toType) {
        return XUtils.areTypesEquivalent(this, toType);
    }

    // Used for debugging
    protected abstract String getDisplayName();

    @Override
    public String toString() {
        return getDisplayName();
    }
}

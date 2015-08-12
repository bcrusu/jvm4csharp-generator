package com.jvm4csharp.generator.reflectx;

import java.lang.reflect.WildcardType;
import java.util.HashSet;
import java.util.Set;

//TODO: does it require replaceTypeVariable override?
public class XWildcardType extends XType {
    private final WildcardType _wildcardType;

    XWildcardType(WildcardType wildcardType) {
        _wildcardType = wildcardType;
    }

    @Override
    public Set<String> getReferencedPackageNames() {
        return new HashSet<>();
    }

    @Override
    public XTypeCompareResult compareTo(XType other) {
        if (!(other instanceof XWildcardType))
            return XTypeCompareResult.NotEqual;

        XWildcardType other2 = (XWildcardType) other;
        if (_wildcardType == other2._wildcardType)
            return XTypeCompareResult.Equal;

        return XTypeCompareResult.NotEqual;
    }

    @Override
    protected String getDisplayName() {
        return "?";
    }
}

package com.jvm4csharp.generator;

import com.jvm4csharp.generator.reflectx.IClassMemberFilter;
import com.jvm4csharp.generator.reflectx.XConstructor;
import com.jvm4csharp.generator.reflectx.XField;
import com.jvm4csharp.generator.reflectx.XMethod;

class DefaultClassMemberFilter implements IClassMemberFilter {
    private final String[] _includedPackages;

    public DefaultClassMemberFilter(String[] includedPackages) {
        _includedPackages = includedPackages;
    }

    @Override
    public boolean isIncluded(XField field) {
        boolean result = !Utils.hasOtherPackageReferences(_includedPackages, field.getReferencedPackages());

        if (!result)
            System.out.println(String.format("\tField '%1s' was excluded from generation.", field));

        return result;
    }

    @Override
    public boolean isIncluded(XConstructor constructor) {
        boolean result = !Utils.hasOtherPackageReferences(_includedPackages, constructor.getReferencedPackages());

        if (!result)
            System.out.println(String.format("\tConstructor '%1s' was excluded from generation.", constructor));

        return result;
    }

    @Override
    public boolean isIncluded(XMethod method) {
        boolean result = !Utils.hasOtherPackageReferences(_includedPackages, method.getReferencedPackages());

        if (!result)
            System.out.println(String.format("\tMethod '%1s' was excluded from generation.", method));

        return result;
    }
}

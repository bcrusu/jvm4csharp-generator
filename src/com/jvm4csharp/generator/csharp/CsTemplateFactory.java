package com.jvm4csharp.generator.csharp;

import com.jvm4csharp.generator.ReflectionHelper;

public class CsTemplateFactory {
    public static boolean canCreateTemplate(Class clazz) {
        if (!ReflectionHelper.isPublic(clazz))
            return false;

        // cannot create proxies for primitive types nor for arrays
        if (clazz.isPrimitive() || clazz.isArray())
            return false;

        // nested classes are handled inside CsClassTemplate
        if (clazz.isLocalClass() || clazz.isMemberClass())
            return false;

        // anonymous/compiler-generated classes will be ignored
        if (clazz.isSynthetic())
            return false;

        return true;
    }

    public static ICsTemplate createTemplate(Class clazz) {
        if (clazz.isEnum())
            return new CsEnumTemplate(clazz);
        else if (clazz.isInterface())
            return new CsInterfaceTemplate(clazz);

        return new CsClassTemplate(clazz);
    }
}

package com.jvm4csharp.generator.csharp;

import com.jvm4csharp.generator.ReflectionHelper;

public class CsTemplateFactory {
    public static boolean canCreateTemplate(Class clazz) {
        if (!ReflectionHelper.isPublic(clazz))
            return false;

        // cannot create proxies for primitive types nor for arrays
        if (clazz.isPrimitive() || clazz.isArray())
            return false;

        // anonymous/compiler-generated classes will be ignored
        if (clazz.isSynthetic() || clazz.isLocalClass() || clazz.isAnonymousClass())
            return false;

        return true;
    }

    public static ICsTemplate createTemplate(Class clazz) {
        if (clazz.isEnum())
            return new CsEnumTemplate(clazz);
        else if (clazz.isInterface()) {
            return new CsInterfaceTemplate(clazz);

            //if (interfaceNeedsCompanionClass(clazz))
            //TODO:
        }

        return new CsClassTemplate(clazz);
    }

    private static boolean interfaceNeedsCompanionClass(Class clazz) {
        if (!clazz.isInterface())
            return false;

        long staticFieldsCount = ReflectionHelper.getPublicDeclaredFields(clazz)
                .stream()
                .filter(ReflectionHelper::isStatic)
                .count();

        if (staticFieldsCount > 0)
            return true;

        long staticAndDefaultMethodCount = ReflectionHelper.getPublicDeclaredMethods(clazz)
                .stream()
                .filter(x -> ReflectionHelper.isStatic(x) || x.isDefault())
                .count();

        if (staticAndDefaultMethodCount > 0)
            return true;

        return false;
    }
}

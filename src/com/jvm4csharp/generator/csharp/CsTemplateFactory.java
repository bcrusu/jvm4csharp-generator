package com.jvm4csharp.generator.csharp;

import com.jvm4csharp.generator.ClassDetails;
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

    public static ICsTemplate createTemplate(ClassDetails classDetails) {
        Class clazz = classDetails.Class;

        if (clazz.isEnum())
            return new CsEnumTemplate(classDetails);
        else if (clazz.isInterface()) {
            return new CsInterfaceTemplate(classDetails);

            //if (interfaceNeedsCompanionClass(clazz))
            //TODO:
        }

        return new CsClassTemplate(classDetails);
    }

    private static boolean interfaceNeedsCompanionClass(ClassDetails classDetails) {
        if (!classDetails.Class.isInterface())
            return false;

        long staticFieldsCount = classDetails.getFields()
                .stream()
                .filter(ReflectionHelper::isStatic)
                .count();

        if (staticFieldsCount > 0)
            return true;

        long staticAndDefaultMethodCount = classDetails.getMethods()
                .stream()
                .filter(x -> ReflectionHelper.isStatic(x) || x.isDefault())
                .count();

        if (staticAndDefaultMethodCount > 0)
            return true;

        return false;
    }
}

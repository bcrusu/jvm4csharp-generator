package com.jvm4csharp.generator.csharp;

import com.jvm4csharp.generator.ReflectionHelper;

import java.util.LinkedList;

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

    public static ICsTemplate[] createTemplates(Class clazz) {
        LinkedList<ICsTemplate> result = new LinkedList<>();

        if (clazz.isEnum())
            result.add(new CsEnumTemplate());
        else if (clazz.isInterface()) {
            result.add(new CsInterfaceTemplate());
            //TODO: add ConcreteProxyType template
        } else
            result.add(new CsClassTemplate(clazz));

        return result.toArray(new ICsTemplate[result.size()]);
    }
}

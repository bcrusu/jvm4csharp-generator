package com.jvm4csharp.generator.csharp;

import com.jvm4csharp.generator.reflectx.XClass;
import com.jvm4csharp.generator.reflectx.XClassDefinition;
import com.jvm4csharp.generator.reflectx.XField;

public class CsTemplateFactory {
    public static ICsTemplate createTemplate(XClassDefinition classDefinition) {
        XClass xClass = classDefinition.getXClass();

        if (xClass.isEnum())
            return new CsEnumTemplate(classDefinition);
        else if (xClass.isInterface()) {
            return new CsInterfaceTemplate(classDefinition);

            //if (interfaceNeedsCompanionClass(clazz))
            //TODO:
        }

        return new CsClassTemplate(classDefinition);
    }

    private static boolean interfaceNeedsCompanionClass(XClassDefinition classDefinition) {
        XClass xClass = classDefinition.getXClass();

        if (!xClass.isInterface())
            return false;

        boolean hasStaticFields = classDefinition.getFields()
                .stream()
                .anyMatch(XField::isStatic);

        if (hasStaticFields)
            return true;

        boolean hasStaticOrDefaultMethods = classDefinition.getDeclaredMethods()
                .stream()
                .anyMatch(x -> x.isStatic() || x.isDefault());

        return hasStaticOrDefaultMethods;
    }
}

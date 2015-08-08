package com.jvm4csharp.generator.csharp;

import com.jvm4csharp.generator.GenerationResult;
import com.jvm4csharp.generator.ReflectionHelper;
import com.jvm4csharp.generator.TemplateHelper;

import java.lang.reflect.GenericDeclaration;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CsTemplateHelper {
    public static void renderTypeParameters(GenerationResult result, GenericDeclaration genericDeclaration) {
        List<CsType> typeParameters = Arrays.asList(genericDeclaration.getTypeParameters())
                .stream()
                .map(CsType::getCsType)
                .collect(Collectors.toList());

        if (typeParameters.size() > 0) {
            result.append("<");
            for (int i = 0; i < typeParameters.size(); i++) {
                CsType typeParametersCsType = typeParameters.get(i);
                result.append(typeParametersCsType.displayName);

                if (i < typeParameters.size() - 1)
                    result.append(", ");
            }
            result.append(">");
        }
    }

    public static void renderImplementedInterfaces(GenerationResult result, Class _class, List<CsType> implementedInterfaces) {
        if (implementedInterfaces.size() > 0) {
            if (_class.isInterface())
                result.append(" : ");
            else
                result.append(", ");

            for (int i = 0; i < implementedInterfaces.size(); i++) {
                CsType implementedInterfaceCsType = implementedInterfaces.get(i);
                result.append(implementedInterfaceCsType.displayName);

                if (i < implementedInterfaces.size() - 1)
                    result.append(", ");
            }
        }
    }

    public static void renderConstructors(GenerationResult result, Class _class, List<CsConstructorTemplate> constructors) {
        boolean isFinal = ReflectionHelper.isFinal(_class);
        boolean isAbstract = ReflectionHelper.isAbstract(_class);
        String csClassName = CsType.getCsClassName(_class);

        if (!isFinal) {
            result.append(TemplateHelper.TAB);
            result.append("protected ");
            result.append(csClassName);
            result.appendNewLine("(JavaVoid v) : base(v) { }");
        } else {
            if (constructors.size() == 0) {
                result.append(TemplateHelper.TAB);
                result.append("private ");
                result.append(csClassName);
                result.appendNewLine("() : base(JavaVoid.Void) { }");
            }
        }

        if (!isAbstract) {
            for (ICsTemplate template : constructors)
                template.generate().renderTo(result, 1);
        }
    }
}

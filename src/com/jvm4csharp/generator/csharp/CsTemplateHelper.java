package com.jvm4csharp.generator.csharp;

import com.jvm4csharp.generator.GenerateResult;

import java.lang.reflect.GenericDeclaration;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CsTemplateHelper {
    public static void renderTypeParameters(GenerateResult result, GenericDeclaration genericDeclaration) {
        List<CsType> typeParameters = Arrays.asList(genericDeclaration.getTypeParameters())
                .stream()
                .map(CsConverter::getCsType)
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

    public static void renderImplementedInterfaces(GenerateResult result, Class _class, List<CsType> implementedInterfaces) {
        if (implementedInterfaces.size() > 0) {
            if (_class.isInterface())
                result.append(" : ");
            else
                result.append(" , ");

            for (int i = 0; i < implementedInterfaces.size(); i++) {
                CsType implementedInterfaceCsType = implementedInterfaces.get(i);
                result.append(implementedInterfaceCsType.displayName);

                if (i < implementedInterfaces.size() - 1)
                    result.append(", ");
            }
        }
    }
}

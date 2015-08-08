package com.jvm4csharp.generator.csharp;

import com.jvm4csharp.generator.GenerationResult;
import com.jvm4csharp.generator.ReflectionHelper;
import com.jvm4csharp.generator.TemplateHelper;

import java.lang.reflect.Executable;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class CsTemplateHelper {
    private static Set<String> CsKeywords = new HashSet<>();

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
            if (_class.isInterface()) {
                result.append(" : ");
                if (implementedInterfaces.size() == 0)
                    result.append("IJavaObject");
            } else
                result.append(", ");

            for (int i = 0; i < implementedInterfaces.size(); i++) {
                CsType implementedInterfaceCsType = implementedInterfaces.get(i);
                result.append(implementedInterfaceCsType.displayName);

                if (i < implementedInterfaces.size() - 1)
                    result.append(", ");
            }
        }
    }

    public static void renderBaseClass(GenerationResult result, Class clazz, CsType superclassCsType) {
        if (clazz == Object.class)
            return;

        result.append(" : ");

        if (clazz == Throwable.class) {
            result.append("global::System.Exception");
            return;
        }

        result.append(superclassCsType.displayName);
    }

    public static void renderConstructors(GenerationResult result, Class clazz, List<CsConstructorTemplate> constructors) {
        boolean isFinal = ReflectionHelper.isFinal(clazz);
        boolean isAbstract = ReflectionHelper.isAbstract(clazz);
        String csClassName = CsType.getCsClassName(clazz);

        boolean addedProxyCtor = false;
        if (!isFinal) {
            if (clazz != Object.class && clazz != Throwable.class) {
                result.append(TemplateHelper.TAB);
                result.append("protected ");
                result.append(csClassName);
                result.appendNewLine("(JavaVoid v) : base(v) { }");
                addedProxyCtor = true;
            }
        } else {
            if (constructors.size() == 0) {
                result.append(TemplateHelper.TAB);
                result.append("private ");
                result.append(csClassName);
                result.appendNewLine("() : base(JavaVoid.Void) { }");
                addedProxyCtor = true;
            }
        }

        if (!isAbstract) {
            if (addedProxyCtor)
                result.newLine();

            for (ICsTemplate template : constructors)
                template.generate().renderTo(result, 1);
        }
    }

    public static String[] getCsParameterNames(Executable executable){
        Parameter[] parameters = executable.getParameters();
        String[] result = new String[parameters.length];

        for (int i = 0; i < parameters.length; i++) {
            String parameterName = parameters[i].getName();
            result[i] = CsTemplateHelper.escapeCsKeyword(parameterName);
        }

        return result;
    }

    public static String escapeCsKeyword(String str) {
        if (CsKeywords.contains(str))
            return "@" + str;

        return str;
    }

    static {
        String[] keywords = new String[]{"abstract", "add", "alias", "as", "ascending", "async", "await", "base", "bool", "break", "byte",
                "case", "catch", "char", "checked", "class", "const", "continue", "decimal", "default", "delegate", "descending", "do",
                "double", "dynamic", "else", "enum", "event", "explicit", "extern", "false", "finally", "fixed", "float", "for", "foreach",
                "from", "get", "global", "goto", "group", "if", "implicit", "in", "int", "interface", "internal", "into", "is", "join", "let",
                "lock", "long", "namespace", "new", "null", "object", "operator", "orderby", "out", "override", "params", "partial", "private",
                "protected", "public", "readonly", "ref", "remove", "return", "sbyte", "sealed", "select", "set", "short", "sizeof", "stackalloc",
                "static", "string", "struct", "switch", "this", "throw", "true", "try", "typeof", "uint", "ulong", "unchecked", "unsafe", "ushort",
                "using", "value", "var", "virtual", "void", "volatile", "where", "while", "yield"};

        CsKeywords.addAll(Arrays.asList(keywords));
    }
}

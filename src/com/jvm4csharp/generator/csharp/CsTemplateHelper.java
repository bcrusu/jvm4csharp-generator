package com.jvm4csharp.generator.csharp;

import com.jvm4csharp.generator.GenerationResult;
import com.jvm4csharp.generator.TemplateHelper;
import com.jvm4csharp.generator.reflectx.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CsTemplateHelper {
    private static Set<String> CsKeywords = new HashSet<>();

    public static void renderTypeParameters(GenerationResult result, XGenericDeclaration genericDeclaration) {
        List<XTypeVariable> typeParameters = genericDeclaration.getTypeParameters();

        if (typeParameters.size() > 0) {
            result.append("<");
            for (int i = 0; i < typeParameters.size(); i++) {
                result.append(CsType.getDisplayName(typeParameters.get(i)));

                if (i < typeParameters.size() - 1)
                    result.append(", ");
            }
            result.append(">");
        }
    }

    public static void renderImplementedInterfaces(GenerationResult result, XClassDefinition classDefinition) {
        List<XClassDefinition> implementedInterfaces = classDefinition.getImplementedInterfaces();

        if (classDefinition.getXClass().isInterface()) {
            result.append(" : ");
            if (implementedInterfaces.size() == 0)
                result.append("IJavaObject");
        }

        if (implementedInterfaces.size() > 0) {
            if (!classDefinition.getXClass().isInterface())
                result.append(", ");

            for (int i = 0; i < implementedInterfaces.size(); i++) {
                result.append(CsType.getDisplayName(implementedInterfaces.get(i)));

                if (i < implementedInterfaces.size() - 1)
                    result.append(", ");
            }
        }
    }

    public static void renderBaseClass(GenerationResult result, XClassDefinition classDefinition) {
        if (classDefinition.getXClass().isClass(Object.class))
            return;

        result.append(" : ");

        if (classDefinition.getXClass().isClass(Throwable.class)) {
            result.append("global::System.Exception");
            return;
        }

        XClassDefinition superclass = classDefinition.getSuperclass();
        result.append(CsType.getDisplayName(superclass));
    }

    public static void renderConstructors(GenerationResult result, XClassDefinition classDefinition) {
        List<XConstructor> constructors = classDefinition.getConstructors();
        String csClassName = CsType.getSimpleClassName(classDefinition.getXClass());

        if (!classDefinition.getXClass().isFinal()) {
            if (!classDefinition.getXClass().isClass(Object.class) && !classDefinition.getXClass().isClass(Throwable.class)) {
                result.append(TemplateHelper.TAB);
                result.append("protected ");
                result.append(csClassName);
                result.appendNewLine("(JavaVoid v) : base(v) {}");
            }
        } else {
            if (constructors.size() == 0) {
                result.append(TemplateHelper.TAB);
                result.append("private ");
                result.append(csClassName);
                result.appendNewLine("() : base(JavaVoid.Void) {}");
            }
        }

        if (!classDefinition.getXClass().isAbstract()) {
            for (int i = 0; i < constructors.size(); i++) {
                result.newLine();
                XConstructor constructor = constructors.get(i);
                ICsTemplate template = new CsConstructorTemplate(constructor);
                template.generate().renderTo(result, 1);
            }
        }
    }

    public static void renderMethods(GenerationResult result, XClassDefinition classDefinition, List<XMethod> methods) {
//        for (int i = 0; i < methods.size(); i++) {
//            XMethod method = methods.get(i);
//            ICsTemplate template = new CsMethodTemplate(method);
//            template.generate().renderTo(result, 1);
//
//            if (i < methods.size() - 1) {
//                result.newLine();
//            }
//        }
    }

    //TODO: review
    public static void renderMethods(GenerationResult result, XClassDefinition classDefinition) {
        XClassDefinition superclass = classDefinition.getSuperclass();
        List<XClassDefinition> superclassImplementedInterfaces = superclass.getImplementedInterfaces();

        List<XClassDefinition> implementedInterfaces = classDefinition.getImplementedInterfaces();
        List<XMethod> declaredMethods = classDefinition.getDeclaredMethods();



    }

    public static void renderFields(GenerationResult result, XClassDefinition classDefinition, List<XField> fields) {
        for (int i = 0; i < fields.size(); i++) {
            XField field = fields.get(i);
            ICsTemplate template = new CsPropertyTemplate(field);
            template.generate().renderTo(result, 1);

            if (i < fields.size() - 1)
                result.newLine();
        }
    }

    public static void renderFields(GenerationResult result, XClassDefinition classDefinition) {
        renderFields(result, classDefinition, classDefinition.getFields());
    }

    public static String[] getEscapedParameterNames(XExecutable executable) {
        List<String> names = executable.getParameterNames();
        String[] result = new String[names.size()];

        for (int i = 0; i < names.size(); i++)
            result[i] = CsTemplateHelper.escapeCsKeyword(names.get(i));

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

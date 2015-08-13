package com.jvm4csharp.generator.csharp;

import com.jvm4csharp.generator.GenerationResult;
import com.jvm4csharp.generator.TemplateHelper;
import com.jvm4csharp.generator.reflectx.*;

import java.util.*;

public class CsTemplateHelper {
    private static Set<String> CsKeywords = new HashSet<>();

    public static void renderTypeParameters(GenerationResult result, IGenericDeclaration genericDeclaration) {
        List<XTypeVariable> typeParameters = genericDeclaration.getTypeParameters();

        if (typeParameters.size() > 0) {
            result.append("<");
            for (int i = 0; i < typeParameters.size(); i++) {
                result.append(CsType.renderType(typeParameters.get(i)));

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
                result.append(CsType.renderTypeDefinition(implementedInterfaces.get(i)));

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

        if (superclass.getXClass().isClass(Object.class)) {
            result.append("Object");
            return;
        }

        result.append(CsType.renderTypeDefinition(superclass));
    }

    public static void renderConstructors(GenerationResult result, XClassDefinition classDefinition) {
        List<XConstructor> constructors = classDefinition.getConstructors();
        String csClassName = CsType.renderSimpleTypeName(classDefinition);

        boolean addedProxyCtor = false;
        if (!classDefinition.getXClass().isFinal()) {
            if (!classDefinition.getXClass().isClass(Object.class) && !classDefinition.getXClass().isClass(Throwable.class)) {
                result.append(TemplateHelper.TAB);
                result.append("protected ");
                result.append(csClassName);
                result.append("(JavaVoid v) : base(v) {}");
                addedProxyCtor = true;
            }
        } else {
            if (constructors.size() == 0) {
                result.append(TemplateHelper.TAB);
                result.append("private ");
                result.append(csClassName);
                result.append("() : base(JavaVoid.Void) {}");
                addedProxyCtor = true;
            }
        }

        result.ensureEmptyLine(addedProxyCtor);

        if (!classDefinition.getXClass().isAbstract()) {
            for (int i = 0; i < constructors.size(); i++) {
                result.newLine();
                XConstructor constructor = constructors.get(i);
                ICsTemplate template = new CsConstructorTemplate(constructor);
                template.generate().renderTo(result, 1);
            }
        }
    }

    public static void renderTypeParameterConstraints(GenerationResult result, IGenericDeclaration genericDeclaration) {
        List<XTypeVariable> typeParameters = genericDeclaration.getTypeParameters();
        for (XTypeVariable typeParameter : typeParameters) {
            List<XType> bounds = typeParameter.getBounds();
            if (bounds.size() == 0)
                continue;

            result.newLine();
            result.append(TemplateHelper.TAB);
            result.append("where ");
            result.append(typeParameter.getName());
            result.append(" : ");

            for (int i = 0; i < bounds.size(); i++) {
                XType bound = bounds.get(i);
                result.append(CsType.renderType(bound));

                if (i < bounds.size() - 1)
                    result.append(", ");
            }
        }
    }

    public static void renderMethods(GenerationResult result, XClassDefinition classDefinition) {
        class MethodToRenderInfo {
            public boolean isNew;
            public boolean isExplicit;
        }

        if (classDefinition.getXClass().isInterface()) {
            renderMethods(result, classDefinition, classDefinition.getDeclaredMethods());
        } else {
            List<XClassDefinition> allSuperclassImplementedInterfaces = new LinkedList<>();
            List<XMethod> allSuperclassMethods = new LinkedList<>();

            XClassDefinition superclass = classDefinition.getSuperclass();
            while (superclass != null) {
                List<XClassDefinition> implementedInterfaces = superclass.getImplementedInterfaces();
                allSuperclassImplementedInterfaces.addAll(implementedInterfaces);

                for (XClassDefinition implementedInterface : implementedInterfaces)
                    allSuperclassMethods.addAll(implementedInterface.getDeclaredMethods());

                allSuperclassMethods.addAll(superclass.getDeclaredMethods());
                superclass = superclass.getSuperclass();
            }

            List<XMethod> allMethods = new LinkedList<>();
            List<Boolean> allMethodsIsExplicit = new LinkedList<>();

            for (XMethod method : classDefinition.getDeclaredMethods()) {
                allMethods.add(method);
                allMethodsIsExplicit.add(false);
            }

            for (XClassDefinition implementedInterface : classDefinition.getImplementedInterfaces()) {
                if (allSuperclassImplementedInterfaces.contains(implementedInterface))
                    continue;

                for (XMethod interfaceMethod : implementedInterface.getDeclaredMethods()) {
                    boolean add = true;
                    boolean isExplicit = false;

                    for (XMethod method : allMethods) {
                        if (method.equals(interfaceMethod)) {
                            XTypeCompareResult xTypeCompareResult = method.getReturnType().compareTo(interfaceMethod.getReturnType());
                            if (xTypeCompareResult == XTypeCompareResult.Equal) {
                                add = false;
                                break;
                            }

                            isExplicit = true;
                            break;
                        }
                    }

                    if (add) {
                        allMethods.add(interfaceMethod);
                        allMethodsIsExplicit.add(isExplicit);
                    }
                }
            }

            List<XMethod> methodsToRender = new LinkedList<>();
            List<MethodToRenderInfo> methodsToRenderInfo = new LinkedList<>();

            // calculate isNew
            for (int i = 0; i < allMethods.size(); i++) {
                XMethod method = allMethods.get(i);
                boolean add = true;
                boolean isNew = false;

                for (XMethod superclassMethod : allSuperclassMethods) {
                    if (method.equals(superclassMethod)) {
                        XTypeCompareResult xTypeCompareResult = method.getReturnType().compareTo(superclassMethod.getReturnType());
                        if (xTypeCompareResult == XTypeCompareResult.Equal)
                            add = false;
                        else
                            isNew = true;

                        break;
                    }
                }

                if (add) {
                    methodsToRender.add(method);
                    MethodToRenderInfo methodRenderInfo = new MethodToRenderInfo();
                    methodRenderInfo.isNew = isNew;
                    methodRenderInfo.isExplicit = allMethodsIsExplicit.get(i);
                    methodsToRenderInfo.add(methodRenderInfo);
                }
            }

            // render
            result.ensureEmptyLine(methodsToRender.size() > 0);

            for (int i = 0; i < methodsToRender.size(); i++) {
                XMethod method = methodsToRender.get(i);
                MethodToRenderInfo methodToRenderInfo = methodsToRenderInfo.get(i);

                ICsTemplate template = new CsMethodTemplate(method, classDefinition, methodToRenderInfo.isNew, methodToRenderInfo.isExplicit);
                template.generate().renderTo(result, 1);

                if (i < methodsToRender.size() - 1) {
                    result.newLine();
                }
            }
        }
    }

    public static void renderMethods(GenerationResult result, XClassDefinition classDefinition, List<XMethod> methods) {
        result.ensureEmptyLine(methods.size() > 0);

        for (int i = 0; i < methods.size(); i++) {
            XMethod method = methods.get(i);
            ICsTemplate template = new CsMethodTemplate(method, classDefinition, false, false);
            template.generate().renderTo(result, 1);

            if (i < methods.size() - 1) {
                result.newLine();
            }
        }
    }

    public static void renderFields(GenerationResult result, List<XField> fields) {
        result.ensureEmptyLine(fields.size() > 0);

        for (int i = 0; i < fields.size(); i++) {
            XField field = fields.get(i);
            ICsTemplate template = new CsPropertyTemplate(field);
            template.generate().renderTo(result, 1);

            if (i < fields.size() - 1)
                result.newLine();
        }
    }

    public static void renderFields(GenerationResult result, XClassDefinition classDefinition) {
        renderFields(result, classDefinition.getFields());
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

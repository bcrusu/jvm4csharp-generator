package com.jvm4csharp.generator.csharp;

import com.jvm4csharp.generator.GenerationResult;
import com.jvm4csharp.generator.TemplateHelper;
import com.jvm4csharp.generator.reflectx.*;

import java.util.*;

public class CsTemplateHelper {
    private static Set<String> CsKeywords = new HashSet<>();

    public static void renderJavaProxyAttribute(GenerationResult result, XClass clazz) {
        result.append("[JavaProxy(\"");
        result.append(clazz.getInternalTypeName());
        result.appendNewLine("\")]");
    }

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

    public static void renderConstructors(GenerationResult result, XClassDefinition classDefinition, boolean callMatchingBaseConstructor) {
        XClass xClass = classDefinition.getXClass();
        List<XConstructor> constructors = classDefinition.getConstructors();
        String csClassName = CsType.renderSimpleTypeName(classDefinition);

        GenerationResult tmpResult = new GenerationResult();

        if (!xClass.isClass(Object.class) && !xClass.isClass(Throwable.class)) {
            tmpResult.append("protected ");
            tmpResult.append(csClassName);
            tmpResult.append("(JavaVoid v) : base(v) {}");
        }

        if (!xClass.isAbstract()) {
            for (XConstructor constructor : constructors) {
                tmpResult.ensureEmptyLine();
                renderConstructor(tmpResult, constructor, callMatchingBaseConstructor);
            }
        }

        tmpResult.renderTo(result, 1);
    }

    private static void renderConstructor(GenerationResult result, XConstructor constructor, boolean callMatchingBaseConstructor) {
        if (constructor.hasTypeParameters())
            throw new UnsupportedOperationException("Generic constructor declarations are not supported.");

        String internalSignature = constructor.getInternalSignature();
        List<XType> parameterTypes = constructor.getParameterTypes();
        String[] parameterNames = CsTemplateHelper.getEscapedParameterNames(constructor);
        XClassDefinition declaringClass = constructor.getDeclaringClass();
        XClass clazz = declaringClass.getXClass();

        // signature
        result.append("public");
        result.append(TemplateHelper.SPACE);
        result.append(CsType.renderSimpleTypeName(declaringClass));

        result.append('(');
        for (int i = 0; i < parameterNames.length; i++) {
            result.append(CsType.renderErasedType(parameterTypes.get(i)));
            result.append(TemplateHelper.SPACE);
            result.append(parameterNames[i]);

            if (i < parameterNames.length - 1)
                result.append(", ");
        }
        result.append(")");

        if (callMatchingBaseConstructor) {
            if (parameterNames.length > 0) {
                result.append(" : base(");
                for (int j = 0; j < parameterNames.length; j++) {
                    result.append(parameterNames[j]);

                    if (j < parameterNames.length - 1)
                        result.append(", ");
                }
                result.append(") {}");
            }
        } else {
            if (!clazz.isClass(Object.class) && !clazz.isClass(Throwable.class))
                result.append(" : base(JavaVoid.Void)");

            result.newLine();
            result.appendNewLine(TemplateHelper.BLOCK_OPEN);

            // body
            result.append(TemplateHelper.TAB);
            result.append("CallConstructor(\"");
            result.append(internalSignature);
            result.append("\"");

            for (String csParameterName : parameterNames) {
                result.append(", ");
                result.append(csParameterName);
            }

            result.appendNewLine(");");
            result.append(TemplateHelper.BLOCK_CLOSE);
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

    public static void renderErasedProxyType(GenerationResult result, XClassDefinition classDefinition) {
        XClass xClass = classDefinition.getXClass();
        if (!xClass.hasTypeParameters())
            return;

        result.cleanEndLines();
        result.newLine();
        CsTemplateHelper.renderJavaProxyAttribute(result, xClass);

        result.append("public");
        if (!xClass.isInterface() && xClass.isAbstract())
            result.append(" abstract");
        if (needsPartialKeyword(xClass))
            result.append(" partial");

        if (xClass.isInterface())
            result.append(" interface ");
        else
            result.append(" class ");

        result.append(CsType.renderSimpleTypeName(classDefinition));
        result.append(" : ");
        result.append(CsType.renderErasedType(xClass));

        result.newLine();
        result.appendNewLine(TemplateHelper.BLOCK_OPEN);

        // constructors
        if (!xClass.isInterface()) {
            renderConstructors(result, classDefinition, true);
        }

        result.cleanEndLines();
        result.append(TemplateHelper.BLOCK_CLOSE);
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
            List<XMethod> superclass2Methods = new LinkedList<>();

            XClassDefinition superclass = classDefinition.getSuperclass();
            while (superclass != null) {
                List<XClassDefinition> implementedInterfaces = superclass.getImplementedInterfaces();
                allSuperclassImplementedInterfaces.addAll(implementedInterfaces);

                for (XClassDefinition implementedInterface : implementedInterfaces)
                    superclass2Methods.addAll(implementedInterface.getDeclaredMethods());

                superclass2Methods.addAll(superclass.getDeclaredMethods());
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

                for (XMethod superclassMethod : superclass2Methods) {
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

    public static boolean needsPartialKeyword(XClass clazz) {
        if (clazz.isClass(Object.class) || clazz.isClass(Throwable.class) ||
                clazz.isClass(Class.class) || clazz.isClass(String.class))
            return true;
        return false;
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

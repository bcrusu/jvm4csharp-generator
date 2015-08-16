package com.jvm4csharp.generator.csharp;

import com.jvm4csharp.generator.TemplateHelper;
import com.jvm4csharp.generator.reflectx.*;

import java.util.*;
import java.util.stream.Collectors;

public class CsTemplateHelper {
    private static Set<String> CsKeywords = new HashSet<>();

    public static void renderJavaProxyAttribute(CsGenerationResult result, XClass clazz) {
        result.append("[JavaProxy(\"");
        result.append(clazz.getInternalTypeName());
        result.appendNewLine("\")]");
    }

    public static void renderJavaSignature(CsGenerationResult result, String internalTypeName) {
        result.append("[JavaSignature(\"");
        result.append(internalTypeName);
        result.appendNewLine("\")]");
    }

    public static void renderTypeParameters(CsGenerationResult result, IGenericDeclaration genericDeclaration) {
        List<XTypeVariable> typeParameters = genericDeclaration.getTypeParameters();

        if (typeParameters.size() > 0) {
            result.append("<");
            for (int i = 0; i < typeParameters.size(); i++) {
                CsType.renderType(result, typeParameters.get(i));

                if (i < typeParameters.size() - 1)
                    result.append(", ");
            }
            result.append(">");
        }
    }

    public static void renderImplementedInterfaces(CsGenerationResult result, XClassDefinition classDefinition) {
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
                CsType.renderTypeDefinition(result, implementedInterfaces.get(i));

                if (i < implementedInterfaces.size() - 1)
                    result.append(", ");
            }
        }
    }

    public static void renderBaseClass(CsGenerationResult result, XClassDefinition classDefinition) {
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
            result.addUsedNamespace("java.lang");
            return;
        }

        CsType.renderTypeDefinition(result, superclass);
    }

    public static void renderConstructors(CsGenerationResult result, XClassDefinition classDefinition, boolean callMatchingBaseConstructor) {
        XClass xClass = classDefinition.getXClass();
        List<XConstructor> constructors = classDefinition.getConstructors();

        CsGenerationResult tmpResult = new CsGenerationResult();

        if (!xClass.isClass(Object.class) && !xClass.isClass(Throwable.class)) {
            tmpResult.ensureEmptyLine();
            tmpResult.append("protected ");
            CsType.renderSimpleTypeName(tmpResult, classDefinition);
            tmpResult.append("(JavaVoid v) : base(v) {}");
        }

        if (!xClass.isAbstract()) {
            for (XConstructor constructor : constructors) {
                tmpResult.ensureEmptyLine();
                renderConstructor(tmpResult, constructor, callMatchingBaseConstructor);
            }
        }

        if (!tmpResult.isEmpty())
            result.ensureEmptyLine();

        tmpResult.renderTo(result, 1);
    }

    private static void renderConstructor(CsGenerationResult result, XConstructor constructor, boolean callMatchingBaseConstructor) {
        if (constructor.hasTypeParameters())
            throw new UnsupportedOperationException("Generic constructor declarations are not supported.");

        String internalSignature = constructor.getInternalSignature();
        String[] parameterNames = CsTemplateHelper.getEscapedParameterNames(constructor);
        XClassDefinition declaringClass = constructor.getDeclaringClass();
        XClass clazz = declaringClass.getXClass();

        // signature
        result.append("public");
        result.append(TemplateHelper.SPACE);
        CsType.renderSimpleTypeName(result, clazz);
        renderExecutableParameters(result, constructor, callMatchingBaseConstructor);

        if (callMatchingBaseConstructor) {
            if (parameterNames.length > 0) {
                result.append(" : base");
                renderExecutableCallParameters(result, constructor);
            }
            result.append(" {}");
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

    public static void renderExecutableParameters(CsGenerationResult result, XExecutable executable, boolean erasedParameterTypes) {
        List<XType> parameterTypes = executable.getParameterTypes();
        String[] parameterNames = CsTemplateHelper.getEscapedParameterNames(executable);

        result.append('(');
        for (int i = 0; i < parameterNames.length; i++) {
            if (erasedParameterTypes)
                CsType.renderErasedType(result, parameterTypes.get(i));
            else
                CsType.renderType(result, parameterTypes.get(i));

            result.append(TemplateHelper.SPACE);
            result.append(parameterNames[i]);

            if (i < parameterNames.length - 1)
                result.append(", ");
        }
        result.append(")");
    }

    public static void renderExecutableCallParameters(CsGenerationResult result, XExecutable executable) {
        String[] parameterNames = CsTemplateHelper.getEscapedParameterNames(executable);

        result.append("(");
        for (int j = 0; j < parameterNames.length; j++) {
            result.append(parameterNames[j]);

            if (j < parameterNames.length - 1)
                result.append(", ");
        }
        result.append(")");
    }

    public static void renderTypeParameterConstraints(CsGenerationResult result, IGenericDeclaration genericDeclaration) {
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
                CsType.renderType(result, bound);

                if (i < bounds.size() - 1)
                    result.append(", ");
            }
        }
    }

    public static void renderErasedProxyType(CsGenerationResult result, XClassDefinition classDefinition) {
        XClass xClass = classDefinition.getXClass();
        if (!xClass.hasTypeParameters())
            return;

        result.cleanEndLines();
        result.ensureEmptyLine();
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

        CsType.renderSimpleTypeName(result, classDefinition);
        result.append(" : ");
        CsType.renderErasedType(result, xClass);

        result.newLine();
        result.appendNewLine(TemplateHelper.BLOCK_OPEN);

        // constructors
        if (!xClass.isInterface()) {
            renderConstructors(result, classDefinition, true);
        }

        result.cleanEndLines();
        result.append(TemplateHelper.BLOCK_CLOSE);
    }

    public static void renderClassMethods(CsGenerationResult result, XClassDefinition classDefinition) {
        class MethodToRenderInfo {
            public XMethod method;
            public boolean isNew;
            public boolean isExplicit;

            @Override
            public String toString() {
                return String.format("%1s; isNew: %2s; isExplicit: %3s", method, isNew, isExplicit);
            }
        }

        class tmp {
            private List<XMethod> _allSuperclassMethods;
            private List<XMethod> _allImplementedInterfacesMethods;
            private List<MethodToRenderInfo> _methodsToRender;

            public tmp() {
                _allSuperclassMethods = new LinkedList<>();
                _allImplementedInterfacesMethods = new LinkedList<>();
                _methodsToRender = new LinkedList<>();
            }

            public List<MethodToRenderInfo> getMethodsToRender() {
                _allSuperclassMethods = getSuperclassMethods(classDefinition);
                _allImplementedInterfacesMethods = getImplementedInterfacesMethods(classDefinition);

                loadDeclaredMethods();
                processImplementedInterfaceMethods();
                processSuperclassMethods();

                return _methodsToRender;
            }

            private void processSuperclassMethods() {
                List<MethodToRenderInfo> filteredMethodsToRender = new LinkedList<>();

                for (MethodToRenderInfo info : _methodsToRender) {
                    boolean add = true;

                    for (XMethod superclassMethod : _allSuperclassMethods) {
                        if (info.method.isEquivalent(superclassMethod)) {
                            XType methodReturnType = info.method.getReturnType();
                            XType superclassMethodReturnType = superclassMethod.getReturnType();

                            if (methodReturnType.isEquivalent(superclassMethodReturnType)) {
                                add = false;
                                break;
                            }

                            info.isNew = true;
                            break;
                        }
                    }

                    if (add)
                        filteredMethodsToRender.add(info);
                }

                _methodsToRender = filteredMethodsToRender;
            }

            private void processImplementedInterfaceMethods() {
                for (XMethod interfaceMethod : _allImplementedInterfacesMethods) {
                    boolean add = true;
                    boolean isExplicit = false;

                    for (MethodToRenderInfo info : _methodsToRender) {
                        XMethod method = info.method;
                        if (method.isEquivalent(interfaceMethod)) {
                            XType methodReturnType = method.getReturnType();
                            XType interfaceMethodReturnType = interfaceMethod.getReturnType();

                            boolean isEquivalentReturnType = methodReturnType.isEquivalent(interfaceMethodReturnType);

                            if (classDefinition.getXClass().isInterface()) {
                                info.isNew = true;
                            } else {
                                add = !isEquivalentReturnType;
                                isExplicit = !isEquivalentReturnType;
                            }

                            break;
                        }
                    }

                    if (add && !classDefinition.getXClass().isInterface()) {
                        MethodToRenderInfo info = new MethodToRenderInfo();
                        info.method = interfaceMethod;
                        info.isExplicit = isExplicit;
                        _methodsToRender.add(info);
                    }
                }
            }

            private void loadDeclaredMethods() {
                for (XMethod method : classDefinition.getDeclaredMethods()) {
                    if (method.isStatic() && classDefinition.getXClass().isInterface())
                        continue;

                    MethodToRenderInfo info = new MethodToRenderInfo();
                    info.method = method;
                    info.isExplicit = false;
                    info.isNew = false;
                    _methodsToRender.add(info);
                }
            }

            private List<XMethod> getSuperclassMethods(XClassDefinition classDefinition) {
                if (classDefinition.getXClass().isInterface()) {
                    XClassDefinition objectClassDefinition = XClassDefinitionFactory.createClassDefinition(Object.class);
                    return objectClassDefinition.getDeclaredMethods();
                }

                List<XMethod> result = new LinkedList<>();

                XClassDefinition superclass = classDefinition.getSuperclass();
                while (superclass != null) {
                    result.addAll(superclass.getDeclaredMethods());
                    for (XClassDefinition implementedInterface : superclass.getImplementedInterfaces())
                        result.addAll(getImplementedInterfacesMethods(implementedInterface));

                    superclass = superclass.getSuperclass();
                }

                return result;
            }

            private List<XMethod> getImplementedInterfacesMethods(XClassDefinition classDefinition) {
                List<XMethod> result = new LinkedList<>();

                for (XClassDefinition implementedInterface : classDefinition.getImplementedInterfaces()) {
                    for (XMethod method : implementedInterface.getDeclaredMethods())
                        if (!method.isStatic() && !result.contains(method))
                            result.add(method);

                    result.addAll(getImplementedInterfacesMethods(implementedInterface));
                }

                return result;
            }
        }

        List<MethodToRenderInfo> methodsToRender = new tmp().getMethodsToRender();

        // render
        CsGenerationResult tmpResult = new CsGenerationResult();

        for (MethodToRenderInfo methodToRenderInfo : methodsToRender) {
            tmpResult.ensureEmptyLine();
            renderMethod(tmpResult, classDefinition, methodToRenderInfo.method, methodToRenderInfo.isExplicit, methodToRenderInfo.isNew);
        }

        if (!tmpResult.isEmpty())
            result.ensureEmptyLine();

        tmpResult.renderTo(result, 1);
    }

    public static void renderInterfaceMethods(CsGenerationResult result, XClassDefinition classDefinition, boolean forCompanionClass) {
        if (!classDefinition.getXClass().isInterface())
            throw new IllegalArgumentException("Expected interface definition.");

        List<XMethod> methodsToRender = classDefinition.getDeclaredMethods();

        if (forCompanionClass)
            methodsToRender = methodsToRender.stream()
                    .filter(XMethod::isStatic)
                    .collect(Collectors.toList());
        else
            methodsToRender = methodsToRender.stream()
                    .filter(x -> !x.isStatic())
                    .collect(Collectors.toList());

        CsGenerationResult tmpResult = new CsGenerationResult();

        for (XMethod aMethodsToRender : methodsToRender) {
            tmpResult.ensureEmptyLine();
            renderMethod(tmpResult, classDefinition, aMethodsToRender, false, false);
        }

        if (!tmpResult.isEmpty())
            result.ensureEmptyLine();

        tmpResult.renderTo(result, 1);
    }

    //TODO: use C# 'params' keyword for var args
    public static void renderMethod(CsGenerationResult result, XClassDefinition classDefinition,
                                    XMethod method, boolean isExplicit, boolean isNew) {
        String internalSignature = method.getInternalSignature();
        String[] parameterNames = CsTemplateHelper.getEscapedParameterNames(method);

        renderJavaSignature(result, internalSignature);

        if (!isExplicit && !classDefinition.getXClass().isInterface())
            result.append("public ");
        if (isNew)
            result.append("new ");
        if (method.isStatic())
            result.append("static ");

        CsType.renderType(result, method.getReturnType());
        result.append(TemplateHelper.SPACE);
        if (isExplicit) {
            CsType.renderTypeDefinition(result, method.getDeclaringClass());
            result.append(".");
        }
        result.append(CsTemplateHelper.escapeCsKeyword(method.getName()));

        renderTypeParameters(result, method);
        renderExecutableParameters(result, method, false);

        if (!isExplicit)
            renderTypeParameterConstraints(result, method);

        // body
        if (classDefinition.getXClass().isInterface()) {
            result.append(";");
        } else {
            result.newLine();
            result.appendNewLine(TemplateHelper.BLOCK_OPEN);

            result.append(TemplateHelper.TAB);
            if (!method.isVoidReturnType())
                result.append("return ");

            result.append("Call");
            if (method.isStatic())
                result.append("Static");
            result.append("Method");

            if (!method.isVoidReturnType()) {
                result.append("<");
                CsType.renderType(result, method.getReturnType());
                result.append(">");
            }

            result.append('(');
            if (method.isStatic()) {
                result.append("typeof(");
                CsType.renderTypeDefinition(result, method.getDeclaringClass());
                result.append("), ");
            }
            result.append("\"");
            result.append(method.getName());
            result.append("\", \"");
            result.append(internalSignature);
            result.append("\"");

            for (String csParameterName : parameterNames) {
                result.append(", ");
                result.append(csParameterName);
            }

            result.append(");");

            result.newLine();
            result.append(TemplateHelper.BLOCK_CLOSE);
        }
    }

    public static void renderFields(CsGenerationResult result, XClassDefinition classDefinition) {
        List<XField> fields = classDefinition.getFields();

        CsGenerationResult tmpResult = new CsGenerationResult();

        for (XField field : fields) {
            tmpResult.ensureEmptyLine();
            renderField(tmpResult, field);
        }

        if (!tmpResult.isEmpty())
            result.ensureEmptyLine();

        tmpResult.renderTo(result, 1);
    }

    private static void renderField(CsGenerationResult result, XField field) {
        String internalTypeName = field.getInternalTypeName();

        renderJavaSignature(result, internalTypeName);

        result.append("public ");
        if (field.isStatic())
            result.append("static ");

        CsType.renderType(result, field.getType());
        result.append(TemplateHelper.SPACE);
        result.append(CsTemplateHelper.escapeCsKeyword(field.getName()));

        result.newLine();
        result.appendNewLine(TemplateHelper.BLOCK_OPEN);

        // getter
        result.append(TemplateHelper.TAB);
        result.append("get { return Get");
        if (field.isStatic())
            result.append("Static");
        result.append("Field<");
        CsType.renderType(result, field.getType());
        result.append(">(");

        if (field.isStatic()) {
            result.append("typeof(");
            CsType.renderUnboundType(result, field.getDeclaringClass());
            result.append("), ");
        }

        result.append("\"");
        result.append(field.getName());
        result.append("\", \"");
        result.append(internalTypeName);
        result.append("\"); }");

        // setter
        if (!field.isFinal()) {
            result.newLine();
            result.append(TemplateHelper.TAB);

            result.append("set { Set");
            if (field.isStatic())
                result.append("Static");
            result.append("Field(");
            if (field.isStatic()) {
                result.append("typeof(");
                CsType.renderUnboundType(result, field.getDeclaringClass());
                result.append("), ");
            }
            result.append("\"");
            result.append(field.getName());
            result.append("\", \"");
            result.append(internalTypeName);
            result.append("\", value); }");
        }

        result.newLine();
        result.append(TemplateHelper.BLOCK_CLOSE);
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
        return clazz.isClass(Object.class) || clazz.isClass(Throwable.class) ||
                clazz.isClass(Class.class) || clazz.isClass(String.class);
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

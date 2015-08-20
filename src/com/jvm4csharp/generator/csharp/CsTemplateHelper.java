package com.jvm4csharp.generator.csharp;

import com.jvm4csharp.generator.TemplateHelper;
import com.jvm4csharp.generator.reflectx.*;

import java.util.*;
import java.util.stream.Collectors;

class CsTemplateHelper {
    private static Set<String> CsKeywords = new HashSet<>();

    public static void renderClassDefinition(CsGenerationResult result, XClassDefinition classDefinition) {
        XClass xClass = classDefinition.getXClass();
        boolean isInterface = xClass.isInterface();

        renderJavaProxyAttribute(result, xClass);

        if (isInterface) {
            result.append("public partial interface ");
        } else {
            result.append("public");
            if (xClass.isAbstract())
                result.append(" abstract");

            result.append(" partial class ");
        }

        result.append(xClass.getSimpleName());
        renderTypeParameters(result, classDefinition);

        if (!isInterface)
            renderBaseClass(result, classDefinition);

        renderImplementedInterfaces(result, classDefinition);
        renderTypeParameterConstraints(result, classDefinition);

        result.newLine();
        result.appendNewLine(TemplateHelper.BLOCK_OPEN);

        if (!isInterface)
            renderConstructors(result, classDefinition);

        result.ensureEmptyLine();
        renderFields(result, classDefinition);

        result.ensureEmptyLine();
        renderClassMethods(result, classDefinition);

        if (!isInterface && !xClass.hasTypeParameters()) {
            result.ensureEmptyLine();
            renderNestedClasses(result, classDefinition);
        }

        result.cleanEndLines();
        result.append(TemplateHelper.BLOCK_CLOSE);

        renderClassCompanion(result, classDefinition);
        renderInterfaceCompanion(result, classDefinition);
    }

    private static void renderJavaProxyAttribute(CsGenerationResult result, XClass clazz) {
        String internalTypeName = clazz.getInternalTypeName();

        result.append("[JavaProxy(\"");
        result.append(internalTypeName.substring(1, internalTypeName.length() - 1));
        result.appendNewLine("\")]");
    }

    private static void renderJavaSignature(CsGenerationResult result, String internalTypeName) {
        result.append("[JavaSignature(\"");
        result.append(internalTypeName);
        result.appendNewLine("\")]");
    }

    private static void renderTypeParameters(CsGenerationResult result, IGenericDeclaration genericDeclaration) {
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

    private static void renderActualTypeArguments(CsGenerationResult result, XClassDefinition classDefinition) {
        List<XType> actualTypeArguments = classDefinition.getActualTypeArguments();
        if (actualTypeArguments.size() > 0) {
            result.append("<");
            for (int i = 0; i < actualTypeArguments.size(); i++) {
                XType actualTypeArgument = actualTypeArguments.get(i);
                CsType.renderType(result, actualTypeArgument);
                if (i < actualTypeArguments.size() - 1)
                    result.append(", ");
            }
            result.append(">");
        }
    }

    private static void renderImplementedInterfaces(CsGenerationResult result, XClassDefinition classDefinition) {
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
                XClassDefinition implementedInterface = implementedInterfaces.get(i);
                CsType.renderSimpleTypeName(result, implementedInterface);
                renderActualTypeArguments(result, implementedInterface);

                if (i < implementedInterfaces.size() - 1)
                    result.append(", ");
            }
        }
    }

    private static void renderBaseClass(CsGenerationResult result, XClassDefinition classDefinition) {
        if (classDefinition.getXClass().isClass(Object.class))
            return;

        result.append(" : ");

        if (classDefinition.getXClass().isClass(Throwable.class)) {
            result.append("global::System.Exception");
            return;
        }

        if (classDefinition.getXClass().isClass(Enum.class)) {
            result.append("Enum");
            result.addUsedNamespace("java.lang");
            return;
        }

        XClassDefinition superclass = classDefinition.getSuperclass();

        if (superclass.getXClass().isClass(Object.class)) {
            result.append("Object");
            result.addUsedNamespace("java.lang");
            return;
        }

        CsType.renderSimpleTypeName(result, superclass);
        renderActualTypeArguments(result, superclass);
    }

    private static void renderConstructors(CsGenerationResult result, XClassDefinition classDefinition) {
        XClass xClass = classDefinition.getXClass();
        List<XConstructor> constructors = classDefinition.getConstructors();

        CsGenerationResult tmpResult = new CsGenerationResult();

        if (!xClass.isClass(Object.class) && !xClass.isClass(Throwable.class)) {
            tmpResult.ensureEmptyLine();
            tmpResult.append("protected ");
            tmpResult.append(xClass.getSimpleName());
            tmpResult.append("(ProxyCtor p) : base(p) {}");
        }

        if (!xClass.isAbstract()) {
            for (XConstructor constructor : constructors) {
                tmpResult.ensureEmptyLine();
                renderConstructor(tmpResult, constructor);
            }
        }

        if (!tmpResult.isEmpty())
            result.ensureEmptyLine();

        tmpResult.renderTo(result, 1);
    }

    private static void renderConstructor(CsGenerationResult result, XConstructor constructor) {
        if (constructor.hasTypeParameters())
            throw new UnsupportedOperationException("Generic constructor declarations are not supported.");

        String internalSignature = constructor.getInternalSignature();
        String[] parameterNames = getEscapedParameterNames(constructor);
        XClassDefinition declaringClass = constructor.getDeclaringClass();
        XClass xClass = declaringClass.getXClass();

        // signature
        result.append("public");
        result.append(TemplateHelper.SPACE);
        result.append(xClass.getSimpleName());
        renderExecutableParameters(result, constructor);

        if (!xClass.isClass(Object.class) && !xClass.isClass(Throwable.class))
            result.append(" : base(ProxyCtor.I)");

        result.newLine();
        result.appendNewLine(TemplateHelper.BLOCK_OPEN);

        // body
        result.append(TemplateHelper.TAB);
        result.append("Instance.CallConstructor(\"");
        result.append(internalSignature);
        result.append("\"");

        for (String csParameterName : parameterNames) {
            result.append(", ");
            result.append(csParameterName);
        }

        result.appendNewLine(");");
        result.append(TemplateHelper.BLOCK_CLOSE);
    }

    private static void renderExecutableParameters(CsGenerationResult result, XExecutable executable) {
        List<XType> parameterTypes = executable.getParameterTypes();
        String[] parameterNames = getEscapedParameterNames(executable);

        result.append('(');
        for (int i = 0; i < parameterNames.length; i++) {
            CsType.renderType(result, parameterTypes.get(i));
            result.append(TemplateHelper.SPACE);
            result.append(parameterNames[i]);

            if (i < parameterNames.length - 1)
                result.append(", ");
        }
        result.append(")");
    }

    private static void renderTypeParameterConstraints(CsGenerationResult result, IGenericDeclaration genericDeclaration) {
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

    private static void renderClassMethods(CsGenerationResult result, XClassDefinition classDefinition) {
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

    //TODO: use C# 'params' keyword for var args
    private static void renderMethod(CsGenerationResult result, XClassDefinition classDefinition,
                                     XMethod method, boolean isExplicit, boolean isNew) {
        String internalSignature = method.getInternalSignature();
        String[] parameterNames = getEscapedParameterNames(method);

        renderJavaSignature(result, internalSignature);

        if (method.isStatic() || (!isExplicit && !classDefinition.getXClass().isInterface()))
            result.append("public ");
        if (isNew)
            result.append("new ");
        if (method.isStatic())
            result.append("static ");

        CsType.renderType(result, method.getReturnType());
        result.append(TemplateHelper.SPACE);
        if (isExplicit) {
            CsType.renderSimpleTypeName(result, method.getDeclaringClass());
            renderActualTypeArguments(result, method.getDeclaringClass());
            result.append(".");
        }
        result.append(escapeCsKeyword(method.getName()));

        renderTypeParameters(result, method);
        renderExecutableParameters(result, method);

        if (!isExplicit)
            renderTypeParameterConstraints(result, method);

        // body
        if (classDefinition.getXClass().isInterface() && !method.isStatic()) {
            result.append(";");
        } else {
            result.newLine();
            result.appendNewLine(TemplateHelper.BLOCK_OPEN);

            result.append(TemplateHelper.TAB);
            if (!method.isVoidReturnType())
                result.append("return ");

            if (method.isStatic())
                result.append("Static.");
            else
                result.append("Instance.");

            result.append("CallMethod");

            if (!method.isVoidReturnType()) {
                result.append("<");
                CsType.renderType(result, method.getReturnType());
                result.append(">");
            }

            result.append('(');
            if (method.isStatic()) {
                renderTypeofExpression(result, method.getDeclaringClass());
                result.append(", ");
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

    private static void renderFields(CsGenerationResult result, XClassDefinition classDefinition) {
        List<XField> fields = classDefinition.getFields();
        if (classDefinition.getXClass().isInterface())
            fields = fields.stream()
                    .filter(x -> !x.isStatic())
                    .collect(Collectors.toList());

        CsGenerationResult tmpResult = new CsGenerationResult();

        for (XField field : fields) {
            tmpResult.ensureEmptyLine();
            renderField(tmpResult, field);
        }

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
        result.append(escapeCsKeyword(field.getName()));

        result.newLine();
        result.appendNewLine(TemplateHelper.BLOCK_OPEN);

        // getter
        result.append(TemplateHelper.TAB);
        result.append("get { return ");

        if (field.isStatic())
            result.append("Static.");
        else
            result.append("Instance.");

        result.append("GetField<");
        CsType.renderType(result, field.getType());
        result.append(">(");

        if (field.isStatic()) {
            renderTypeofExpression(result, field.getDeclaringClass());
            result.append(", ");
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

            result.append("set { ");

            if (field.isStatic())
                result.append("Static.");
            else
                result.append("Instance.");

            result.append("SetField(");
            if (field.isStatic()) {
                renderTypeofExpression(result, field.getDeclaringClass());
                result.append(", ");
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

    private static void renderClassCompanion(CsGenerationResult result, XClassDefinition classDefinition) {
        XClass xClass = classDefinition.getXClass();

        if (xClass.isInterface())
            return;
        if (classDefinition.getDeclaredClasses().size() == 0)
            return;
        if (!xClass.hasTypeParameters())
            return;

        result.cleanEndLines();
        result.ensureEmptyLine();

        result.append("public class ");
        result.appendNewLine(xClass.getSimpleName());
        result.appendNewLine(TemplateHelper.BLOCK_OPEN);

        // nested classes
        result.ensureEmptyLine();
        renderNestedClasses(result, classDefinition);

        result.cleanEndLines();
        result.append(TemplateHelper.BLOCK_CLOSE);
    }

    private static void renderInterfaceCompanion(CsGenerationResult result, XClassDefinition classDefinition) {
        XClass xClass = classDefinition.getXClass();

        if (!xClass.isInterface())
            return;
        if (!(classDefinition.getDeclaredMethods().stream().anyMatch(XMethod::isStatic) ||
                classDefinition.getFields().stream().anyMatch(XField::isStatic) ||
                classDefinition.getDeclaredClasses().size() > 0))
            return;

        result.cleanEndLines();
        result.ensureEmptyLine();

        result.append("public static class ");
        result.append(classDefinition.getXClass().getSimpleName());
        result.appendNewLine("_");

        result.appendNewLine(TemplateHelper.BLOCK_OPEN);
        CsGenerationResult tmpResult = new CsGenerationResult();

        tmpResult.append("private static readonly JavaProxyOperations.Static Static = JavaProxyOperations.Static.Singleton;");

        // static fields
        List<XField> fields = classDefinition.getFields()
                .stream()
                .filter(XField::isStatic)
                .collect(Collectors.toList());

        for (XField field : fields) {
            tmpResult.ensureEmptyLine();
            renderField(tmpResult, field);
        }

        // static methods
        List<XMethod> staticMethods = classDefinition.getDeclaredMethods()
                .stream()
                .filter(XMethod::isStatic)
                .collect(Collectors.toList());

        for (XMethod aMethodsToRender : staticMethods) {
            tmpResult.ensureEmptyLine();
            renderMethod(tmpResult, classDefinition, aMethodsToRender, false, false);
        }

        tmpResult.renderTo(result, 1);

        // nested classes
        result.ensureEmptyLine();
        renderNestedClasses(result, classDefinition);

        result.append(TemplateHelper.BLOCK_CLOSE);
    }

    private static void renderNestedClasses(CsGenerationResult result, XClassDefinition classDefinition) {
        List<XClassDefinition> declaredClasses = classDefinition.getDeclaredClasses();

        CsGenerationResult tmpResult = new CsGenerationResult();

        for (XClassDefinition clazz : declaredClasses) {
            tmpResult.ensureEmptyLine();
            renderClassDefinition(tmpResult, clazz);
        }

        tmpResult.renderTo(result, 1);
    }

    private static void renderTypeofExpression(CsGenerationResult result, XClassDefinition classDefinition) {
        int typeParametersCount = classDefinition.getTypeParameters().size();

        result.append("typeof(");
        result.append(classDefinition.getXClass().getSimpleName());

        if (typeParametersCount > 0) {
            result.append("<");
            for (int i = 0; i < typeParametersCount - 1; i++)
                result.append(",");
            result.append(">");
        }

        result.append(")");
    }

    public static String[] getEscapedParameterNames(XExecutable executable) {
        List<String> names = executable.getParameterNames();
        String[] result = new String[names.size()];

        for (int i = 0; i < names.size(); i++)
            result[i] = escapeCsKeyword(names.get(i));

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

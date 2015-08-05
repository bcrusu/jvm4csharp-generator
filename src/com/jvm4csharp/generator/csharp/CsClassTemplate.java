package com.jvm4csharp.generator.csharp;

import com.jvm4csharp.generator.GenerateResult;
import com.jvm4csharp.generator.ReflectionHelper;
import com.jvm4csharp.generator.TemplateHelper;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

//TODO: generics
public class CsClassTemplate implements ICsItemTemplate {
    private final Class _class;
    private final int _indentationLevel;
    private final CsType _classCsType;
    private final CsType _superclassCsType;
    private final List<CsType> _implementedInterfacesCsTypes;

    private final List<CsPropertyTemplate> _fields;
    private final List<CsMethodTemplate> _methods;
    private final List<CsConstructorTemplate> _constructors;
    private final List<CsClassTemplate> _classes;

    public CsClassTemplate(Class clazz, int indentationLevel) {
        _class = clazz;
        _indentationLevel = indentationLevel;
        _classCsType = CsConverter.GetClrType(_class);
        _superclassCsType = CsConverter.GetClrType(_class.getSuperclass());

        _implementedInterfacesCsTypes = ReflectionHelper.getPublicImplementedInterfaces(_class)
                .stream()
                .map(CsConverter::GetClrType)
                .collect(Collectors.toList());

        _fields = ReflectionHelper.getPublicDeclaredFields(_class)
                .stream()
                .map(x -> new CsPropertyTemplate(x, _class))
                .collect(Collectors.toList());

        _methods = ReflectionHelper.getPublicDeclaredMethods(_class)
                .stream()
                .map(x -> new CsMethodTemplate(x, _class))
                .collect(Collectors.toList());

        _constructors = ReflectionHelper.getPublicDeclaredConstructors(_class)
                .stream()
                .map(x -> new CsConstructorTemplate(x, _class))
                .collect(Collectors.toList());

        _classes = ReflectionHelper.getPublicDeclaredClasses(_class)
                .stream()
                .map(x -> new CsClassTemplate(x, indentationLevel + 1))
                .collect(Collectors.toList());
    }

    @Override
    public GenerateResult generate() {
        boolean isFinal = ReflectionHelper.isFinal(_class);

        GenerateResult result = new GenerateResult();

        result.append("public");
        if (isFinal)
            result.append(" sealed");
        result.append(" class ");
        result.append(_classCsType.displayName);
        result.append(" : ");
        result.append(_superclassCsType.displayName);

        for (CsType implementedInterface : _implementedInterfacesCsTypes) {
            result.append(", ");
            result.append(implementedInterface.displayName);
        }
        result.newLine();
        result.appendNewLine(TemplateHelper.BLOCK_OPEN);

        result.append("protected ");
        result.append(_class.getSimpleName());
        result.appendNewLine("(JavaVoid jv) { }");

        LinkedList<GenerateResult> generateResults = new LinkedList<>();

        for (CsConstructorTemplate template : _constructors)
            generateResults.add(template.generate());

        for (CsPropertyTemplate template : _fields)
            generateResults.add(template.generate());

        for (CsMethodTemplate template : _methods)
            generateResults.add(template.generate());

        for (int i = 0; i < generateResults.size(); i++) {
            generateResults.get(i).renderTo(result, _indentationLevel + 1);

            if (i < generateResults.size() - 2)
                result.newLine();
        }

        result.append(TemplateHelper.BLOCK_CLOSE);

        return result;
    }

    @Override
    public CsType[] getReferencedCsTypes() {
        return new CsType[0];
    }
}

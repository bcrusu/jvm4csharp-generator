package com.jvm4csharp.generator.csharp;

import com.jvm4csharp.generator.GenerateResult;
import com.jvm4csharp.generator.ReflectionHelper;
import com.jvm4csharp.generator.TemplateHelper;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class CsClassTemplate implements ICsTemplate {
    private final Class _class;
    private final CsType _classCsType;
    private final CsType _superclassCsType;
    private final List<CsType> _implementedInterfacesCsTypes;

    private final List<CsPropertyTemplate> _fields;
    private final List<CsMethodTemplate> _methods;
    private final List<CsConstructorTemplate> _constructors;
    private final List<ICsTemplate> _nestedTypes;

    public CsClassTemplate(Class clazz) {
        _class = clazz;
        _classCsType = CsConverter.getCsType(_class);
        _superclassCsType = CsConverter.getCsType(_class.getGenericSuperclass());

        _implementedInterfacesCsTypes = ReflectionHelper.getImplementedInterfaces(_class)
                .stream()
                .map(CsConverter::getCsType)
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

        _nestedTypes = ReflectionHelper.getPublicDeclaredClasses(_class)
                .stream()
                .filter(x -> CsTemplateFactory.canCreateTemplate(x))
                .map(x -> CsTemplateFactory.createTemplate(x))
                .collect(Collectors.toList());
    }

    @Override
    public GenerateResult generate() {
        boolean isFinal = ReflectionHelper.isFinal(_class);
        boolean isAbstract = ReflectionHelper.isAbstract(_class);
        String internalTypeName = ReflectionHelper.GetInternalTypeName(_class);

        GenerateResult result = new GenerateResult();

        result.append("[JavaProxy(\"");
        result.append(internalTypeName);
        result.appendNewLine("\")]");

        result.append("public");
        if (isAbstract)
            result.append(" abstract");
        if (isFinal)
            result.append(" sealed");
        result.append(" partial class ");
        result.append(_classCsType.displayName);

        CsTemplateHelper.renderTypeParameters(result, _class);

        result.append(" : ");
        result.append(_superclassCsType.displayName);

        CsTemplateHelper.renderImplementedInterfaces(result, _class, _implementedInterfacesCsTypes);

        result.newLine();
        result.appendNewLine(TemplateHelper.BLOCK_OPEN);

        if (!isFinal) {
            result.append(TemplateHelper.TAB);
            result.append("protected ");
            result.append(_class.getSimpleName());
            result.appendNewLine("(JavaVoid jv) : base(JavaVoid.Void) { }");
            result.newLine();
        }

        LinkedList<GenerateResult> generateResults = new LinkedList<>();

        if (!isAbstract)
            for (ICsTemplate template : _constructors)
                generateResults.add(template.generate());

        for (ICsTemplate template : _fields)
            generateResults.add(template.generate());

        for (ICsTemplate template : _methods)
            generateResults.add(template.generate());

        for (ICsTemplate template : _nestedTypes)
            generateResults.add(template.generate());

        for (int i = 0; i < generateResults.size(); i++) {
            generateResults.get(i).renderTo(result, 1);

            if (i < generateResults.size() - 1)
                result.newLine();
        }

        result.append(TemplateHelper.BLOCK_CLOSE);

        return result;
    }

    @Override
    public CsType[] getReferencedCsTypes() {
        boolean isAbstract = ReflectionHelper.isAbstract(_class);
        LinkedList<CsType> result = new LinkedList<>();

        result.add(_classCsType);
        result.add(_superclassCsType);
        result.addAll(_implementedInterfacesCsTypes);

        if (!isAbstract)
            for (ICsTemplate template : _constructors)
                result.addAll(Arrays.asList(template.getReferencedCsTypes()));

        for (ICsTemplate template : _fields)
            result.addAll(Arrays.asList(template.getReferencedCsTypes()));

        for (ICsTemplate template : _methods)
            result.addAll(Arrays.asList(template.getReferencedCsTypes()));

        for (ICsTemplate template : _nestedTypes)
            result.addAll(Arrays.asList(template.getReferencedCsTypes()));

        return result.toArray(new CsType[result.size()]);
    }
}

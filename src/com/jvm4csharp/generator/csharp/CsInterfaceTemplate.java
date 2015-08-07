package com.jvm4csharp.generator.csharp;

import com.jvm4csharp.generator.GenerateResult;
import com.jvm4csharp.generator.ReflectionHelper;
import com.jvm4csharp.generator.TemplateHelper;

import java.util.*;
import java.util.stream.Collectors;

public class CsInterfaceTemplate implements ICsTemplate {
    private final Class _class;
    private final CsType _classCsType;
    private final List<CsType> _implementedInterfacesCsTypes;
    private final List<CsPropertyTemplate> _fields;
    private final List<CsMethodTemplate> _methods;

    public CsInterfaceTemplate(Class clazz) {
        _class = clazz;
        _classCsType = CsConverter.getCsType(_class);

        _implementedInterfacesCsTypes = ReflectionHelper.getImplementedInterfaces(_class)
                .stream()
                .map(CsConverter::getCsType)
                .collect(Collectors.toList());

        _fields = ReflectionHelper.getPublicDeclaredFields(_class)
                .stream()
                .filter(x -> !ReflectionHelper.isStatic(x))
                .map(x -> new CsPropertyTemplate(x, _class))
                .collect(Collectors.toList());

        _methods = ReflectionHelper.getPublicDeclaredMethods(_class)
                .stream()
                .filter(x -> !ReflectionHelper.isStatic(x))
                .filter(x -> !x.isDefault())
                .map(x -> new CsMethodTemplate(x, _class))
                .collect(Collectors.toList());
    }

    @Override
    public GenerateResult generate() {
        String internalTypeName = ReflectionHelper.GetInternalTypeName(_class);

        GenerateResult result = new GenerateResult();

        result.append("[JavaProxy(\"");
        result.append(internalTypeName);
        result.appendNewLine("\")]");

        result.append("public interface ");
        result.append(_classCsType.displayName);

        CsTemplateHelper.renderTypeParameters(result, _class);
        CsTemplateHelper.renderImplementedInterfaces(result, _class, _implementedInterfacesCsTypes);

        result.newLine();
        result.appendNewLine(TemplateHelper.BLOCK_OPEN);

        LinkedList<GenerateResult> generateResults = new LinkedList<>();

        for (ICsTemplate template : _fields)
            generateResults.addAll(Collections.singletonList(template.generate()));

        for (ICsTemplate template : _methods)
            generateResults.addAll(Collections.singletonList(template.generate()));

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
        LinkedList<CsType> result = new LinkedList<>();

        result.add(_classCsType);
        result.addAll(_implementedInterfacesCsTypes);

        for (ICsTemplate template : _fields)
            result.addAll(Arrays.asList(template.getReferencedCsTypes()));

        for (ICsTemplate template : _methods)
            result.addAll(Arrays.asList(template.getReferencedCsTypes()));

        return result.toArray(new CsType[result.size()]);
    }
}

package com.jvm4csharp.generator.csharp;

import com.jvm4csharp.generator.GenerateResult;
import com.jvm4csharp.generator.ReflectionHelper;
import com.jvm4csharp.generator.TemplateHelper;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

//TODO: generate nested types
public class CsInterfaceTemplate implements ICsTemplate {
    private final Class _class;
    private final CsType _classCsType;
    private final List<CsType> _implementedInterfacesCsTypes;
    private final List<CsPropertyTemplate> _fields;
    private final List<CsMethodTemplate> _methods;

    public CsInterfaceTemplate(Class clazz) {
        _class = clazz;
        _classCsType = CsConverter.GetCsType(_class);

        _implementedInterfacesCsTypes = ReflectionHelper.getPublicImplementedInterfaces(_class)
                .stream()
                .map(CsConverter::GetCsType)
                .collect(Collectors.toList());

        _fields = ReflectionHelper.getPublicDeclaredFields(_class)
                .stream()
                .filter(x -> !x.isSynthetic())
                .map(x -> new CsPropertyTemplate(x, _class))
                .collect(Collectors.toList());

        _methods = ReflectionHelper.getPublicDeclaredMethods(_class)
                .stream()
                .filter(x -> !x.isSynthetic())
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

        result.append(" : IJavaObject");

        for (int i = 0; i < _implementedInterfacesCsTypes.size(); i++) {
            CsType implementedInterface = _implementedInterfacesCsTypes.get(i);
            result.append(implementedInterface.displayName);

            if (i < _implementedInterfacesCsTypes.size() - 1)
                result.append(", ");
        }

        result.newLine();
        result.appendNewLine(TemplateHelper.BLOCK_OPEN);

        LinkedList<GenerateResult> generateResults = new LinkedList<>();

        for (ICsTemplate template : _fields)
            generateResults.addAll(Arrays.asList(template.generate()));

        for (ICsTemplate template : _methods)
            generateResults.addAll(Arrays.asList(template.generate()));

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

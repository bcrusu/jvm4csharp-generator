package com.jvm4csharp.generator.csharp;

import com.jvm4csharp.generator.GenerationResult;
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
        _classCsType = CsType.getCsType(_class);

        _implementedInterfacesCsTypes = ReflectionHelper.getPublicImplementedInterfaces(_class)
                .stream()
                .map(CsType::getCsType)
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
    public GenerationResult generate() {
        String internalTypeName = ReflectionHelper.getInternalTypeName(_class);

        GenerationResult result = new GenerationResult();

        result.append("[JavaProxy(\"");
        result.append(internalTypeName);
        result.appendNewLine("\")]");

        result.append("public interface ");
        result.append(_classCsType.displayName);

        CsTemplateHelper.renderTypeParameters(result, _class);
        CsTemplateHelper.renderImplementedInterfaces(result, _class, _implementedInterfacesCsTypes);

        result.newLine();
        result.appendNewLine(TemplateHelper.BLOCK_OPEN);

        LinkedList<GenerationResult> generationResults = new LinkedList<>();

        for (ICsTemplate template : _fields)
            generationResults.add(template.generate());

        for (ICsTemplate template : _methods)
            generationResults.add(template.generate());

        for (int i = 0; i < generationResults.size(); i++) {
            generationResults.get(i).renderTo(result, 1);

            if (i < generationResults.size() - 1)
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

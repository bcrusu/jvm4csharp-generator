package com.jvm4csharp.generator.csharp;

import com.jvm4csharp.generator.GenerationResult;
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

    public CsClassTemplate(Class clazz) {
        _class = clazz;
        _classCsType = CsType.getCsType(_class);
        _superclassCsType = CsType.getCsType(_class.getGenericSuperclass());

        _implementedInterfacesCsTypes = ReflectionHelper.getPublicImplementedInterfaces(_class)
                .stream()
                .map(CsType::getCsType)
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
    }

    @Override
    public GenerationResult generate() {
        boolean isFinal = ReflectionHelper.isFinal(_class);
        boolean isAbstract = ReflectionHelper.isAbstract(_class);
        String internalTypeName = ReflectionHelper.getInternalTypeName(_class);

        GenerationResult result = new GenerationResult();

        result.append("[JavaProxy(\"");
        result.append(internalTypeName);
        result.appendNewLine("\")]");

        result.append("public");
        if (isAbstract)
            result.append(" abstract");
        if (isFinal)
            result.append(" sealed");
        if (addPartialKeyword(_class))
            result.append(" partial");

        result.append(" class ");
        result.append(_classCsType.displayName);

        CsTemplateHelper.renderTypeParameters(result, _class);
        CsTemplateHelper.renderBaseClass(result, _class, _superclassCsType);
        CsTemplateHelper.renderImplementedInterfaces(result, _class, _implementedInterfacesCsTypes);

        result.newLine();
        result.appendNewLine(TemplateHelper.BLOCK_OPEN);

        CsTemplateHelper.renderConstructors(result, _class, _constructors);

        LinkedList<GenerationResult> generationResults = new LinkedList<>();

        for (ICsTemplate template : _fields)
            generationResults.add(template.generate());

        for (ICsTemplate template : _methods)
            generationResults.add(template.generate());

        if (generationResults.size() > 0) {
            result.newLine();

            for (int i = 0; i < generationResults.size(); i++) {
                generationResults.get(i).renderTo(result, 1);

                if (i < generationResults.size() - 1)
                    result.newLine();
            }
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

        return result.toArray(new CsType[result.size()]);
    }

    private static boolean addPartialKeyword(Class clazz) {
        if (clazz == Object.class ||
                clazz == Throwable.class ||
                clazz == Class.class ||
                clazz == String.class)
            return true;
        return false;
    }
}

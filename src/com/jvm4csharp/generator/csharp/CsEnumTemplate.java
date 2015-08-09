package com.jvm4csharp.generator.csharp;

import com.jvm4csharp.generator.ClassDetails;
import com.jvm4csharp.generator.GenerationResult;
import com.jvm4csharp.generator.ReflectionHelper;
import com.jvm4csharp.generator.TemplateHelper;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class CsEnumTemplate implements ICsTemplate {
    private final ClassDetails _classDetails;
    private final CsType _classCsType;
    private final CsType _superclassCsType;
    private final List<CsType> _implementedInterfacesCsTypes;

    private final List<CsPropertyTemplate> _fields;
    private final List<CsMethodTemplate> _methods;
    private final List<CsConstructorTemplate> _constructors;

    public CsEnumTemplate(ClassDetails classDetails) {
        _classDetails = classDetails;
        _classCsType = CsType.getCsType(classDetails.Class);
        _superclassCsType = CsType.getCsType(classDetails.Class.getGenericSuperclass());

        _implementedInterfacesCsTypes = classDetails.getImplementedInterfaces()
                .stream()
                .map(CsType::getCsType)
                .collect(Collectors.toList());

        _fields = classDetails.getFields()
                .stream()
                .map(x -> new CsPropertyTemplate(x, classDetails))
                .collect(Collectors.toList());

        _methods = classDetails.getMethods()
                .stream()
                .map(x -> new CsMethodTemplate(x, classDetails))
                .collect(Collectors.toList());

        _constructors = classDetails.getConstructors()
                .stream()
                .map(x -> new CsConstructorTemplate(x, classDetails))
                .collect(Collectors.toList());
    }

    @Override
    public GenerationResult generate() {
        String internalTypeName = ReflectionHelper.getInternalTypeName(_classDetails.Class);
        boolean isAbstract = ReflectionHelper.isAbstract(_classDetails.Class);

        GenerationResult result = new GenerationResult();

        result.append("[JavaProxy(\"");
        result.append(internalTypeName);
        result.appendNewLine("\")]");

        result.append("public");
        if (isAbstract)
            result.append(" abstract");
        else
            result.append(" sealed");
        result.append(" class ");
        result.append(_classCsType.displayName);

        CsTemplateHelper.renderBaseClass(result, _classDetails.Class, _superclassCsType);
        CsTemplateHelper.renderImplementedInterfaces(result, _classDetails.Class, _implementedInterfacesCsTypes);

        result.newLine();
        result.appendNewLine(TemplateHelper.BLOCK_OPEN);

        CsTemplateHelper.renderConstructors(result, _classDetails.Class, _constructors);

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
        boolean isAbstract = ReflectionHelper.isAbstract(_classDetails.Class);
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
}

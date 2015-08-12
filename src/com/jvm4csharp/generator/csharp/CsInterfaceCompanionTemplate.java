package com.jvm4csharp.generator.csharp;

import com.jvm4csharp.generator.GenerationResult;
import com.jvm4csharp.generator.TemplateHelper;
import com.jvm4csharp.generator.reflectx.XClassDefinition;
import com.jvm4csharp.generator.reflectx.XField;

import java.util.stream.Collectors;

public class CsInterfaceCompanionTemplate implements ICsTemplate {
    private final XClassDefinition _classDefinition;

    public CsInterfaceCompanionTemplate(XClassDefinition classDefinition) {
        _classDefinition = classDefinition;
    }

    @Override
    public GenerationResult generate() {
        GenerationResult result = new GenerationResult();

        result.append("public static class ");
        result.append(CsType.getDisplayName(_classDefinition));
        result.append("_");

        result.appendNewLine(TemplateHelper.BLOCK_OPEN);

        CsTemplateHelper.renderFields(result, _classDefinition,
                _classDefinition.getFields()
                        .stream()
                        .filter(XField::isStatic)
                        .collect(Collectors.toList()));

        CsTemplateHelper.renderMethods(result, _classDefinition,
                _classDefinition.getDeclaredMethods()
                        .stream()
                        .filter(x -> x.isStatic() || x.isDefault())
                        .collect(Collectors.toList()));

        result.append(TemplateHelper.BLOCK_CLOSE);

        return result;
    }
}

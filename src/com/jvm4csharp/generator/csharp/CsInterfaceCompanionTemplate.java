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
        CsGenerationResult result = new CsGenerationResult();

        result.append("public static class ");
        CsType.renderSimpleTypeName(result, _classDefinition);
        result.append("_");

        result.appendNewLine(TemplateHelper.BLOCK_OPEN);

        CsTemplateHelper.renderFields(result, _classDefinition);
        CsTemplateHelper.renderInterfaceMethods(result, _classDefinition, true);

        result.append(TemplateHelper.BLOCK_CLOSE);

        return result;
    }
}

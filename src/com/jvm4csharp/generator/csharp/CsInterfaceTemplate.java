package com.jvm4csharp.generator.csharp;

import com.jvm4csharp.generator.GenerationResult;
import com.jvm4csharp.generator.TemplateHelper;
import com.jvm4csharp.generator.reflectx.XClassDefinition;

import java.util.stream.Collectors;

public class CsInterfaceTemplate implements ICsTemplate {
    private final XClassDefinition _classDefinition;

    public CsInterfaceTemplate(XClassDefinition classDefinition) {
        _classDefinition = classDefinition;
    }

    @Override
    public GenerationResult generate() {
        String internalTypeName = _classDefinition.getXClass().getInternalTypeName();

        GenerationResult result = new GenerationResult();

        result.append("[JavaProxy(\"");
        result.append(internalTypeName);
        result.appendNewLine("\")]");

        result.append("public interface ");
        result.append(CsType.getDisplayName(_classDefinition));

        CsTemplateHelper.renderTypeParameters(result, _classDefinition);
        CsTemplateHelper.renderImplementedInterfaces(result, _classDefinition);

        result.newLine();
        result.appendNewLine(TemplateHelper.BLOCK_OPEN);

        CsTemplateHelper.renderMethods(result, _classDefinition,
                _classDefinition.getDeclaredMethods()
                        .stream()
                        .filter(x -> !x.isStatic() && !x.isDefault())
                        .collect(Collectors.toList()));

        result.append(TemplateHelper.BLOCK_CLOSE);

        return result;
    }
}

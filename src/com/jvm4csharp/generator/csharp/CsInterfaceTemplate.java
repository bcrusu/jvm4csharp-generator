package com.jvm4csharp.generator.csharp;

import com.jvm4csharp.generator.GenerationResult;
import com.jvm4csharp.generator.TemplateHelper;
import com.jvm4csharp.generator.reflectx.XClass;
import com.jvm4csharp.generator.reflectx.XClassDefinition;

import java.util.stream.Collectors;

public class CsInterfaceTemplate implements ICsTemplate {
    private final XClassDefinition _classDefinition;

    public CsInterfaceTemplate(XClassDefinition classDefinition) {
        _classDefinition = classDefinition;
    }

    @Override
    public GenerationResult generate() {
        XClass xClass = _classDefinition.getXClass();

        GenerationResult result = new GenerationResult();

        CsTemplateHelper.renderJavaProxyAttribute(result, xClass);

        result.append("public interface ");
        result.append(CsType.renderTypeDefinition(_classDefinition));

        CsTemplateHelper.renderTypeParameters(result, _classDefinition);
        CsTemplateHelper.renderImplementedInterfaces(result, _classDefinition);
        CsTemplateHelper.renderTypeParameterConstraints(result, xClass);

        result.newLine();
        result.appendNewLine(TemplateHelper.BLOCK_OPEN);

        CsTemplateHelper.renderMethods(result, _classDefinition,
                _classDefinition.getDeclaredMethods()
                        .stream()
                        .filter(x -> !x.isStatic() && !x.isDefault())
                        .collect(Collectors.toList()));

        result.cleanEndLines();
        result.append(TemplateHelper.BLOCK_CLOSE);

        CsTemplateHelper.renderErasedProxyType(result, _classDefinition);

        return result;
    }
}

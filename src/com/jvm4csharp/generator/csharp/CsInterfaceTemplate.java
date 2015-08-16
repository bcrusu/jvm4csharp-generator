package com.jvm4csharp.generator.csharp;

import com.jvm4csharp.generator.GenerationResult;
import com.jvm4csharp.generator.TemplateHelper;
import com.jvm4csharp.generator.reflectx.XClass;
import com.jvm4csharp.generator.reflectx.XClassDefinition;

public class CsInterfaceTemplate implements ICsTemplate {
    private final XClassDefinition _classDefinition;

    public CsInterfaceTemplate(XClassDefinition classDefinition) {
        _classDefinition = classDefinition;
    }

    @Override
    public GenerationResult generate() {
        XClass xClass = _classDefinition.getXClass();

        CsGenerationResult result = new CsGenerationResult();

        CsTemplateHelper.renderJavaProxyAttribute(result, xClass);

        result.append("public interface ");
        CsType.renderTypeDefinition(result, _classDefinition);

        CsTemplateHelper.renderTypeParameters(result, _classDefinition);
        CsTemplateHelper.renderImplementedInterfaces(result, _classDefinition);
        CsTemplateHelper.renderTypeParameterConstraints(result, xClass);

        result.newLine();
        result.appendNewLine(TemplateHelper.BLOCK_OPEN);

        CsTemplateHelper.renderClassMethods(result, _classDefinition);

        result.cleanEndLines();
        result.append(TemplateHelper.BLOCK_CLOSE);

        CsTemplateHelper.renderErasedProxyType(result, _classDefinition);

        return result;
    }
}

package com.jvm4csharp.generator.csharp;

import com.jvm4csharp.generator.GenerationResult;
import com.jvm4csharp.generator.TemplateHelper;
import com.jvm4csharp.generator.reflectx.XClass;
import com.jvm4csharp.generator.reflectx.XClassDefinition;
import com.jvm4csharp.generator.reflectx.XType;

public class CsClassTemplate implements ICsTemplate {
    private final XClassDefinition _classDefinition;

    public CsClassTemplate(XClassDefinition classDefinition) {
        _classDefinition = classDefinition;
    }

    @Override
    public GenerationResult generate() {
        XClass xClass = _classDefinition.getXClass();

        GenerationResult result = new GenerationResult();

        CsTemplateHelper.renderJavaProxyAttribute(result, xClass);

        result.append("public");
        if (xClass.isAbstract())
            result.append(" abstract");
        if (CsTemplateHelper.needsPartialKeyword(xClass))
            result.append(" partial");

        result.append(" class ");
        result.append(CsType.renderTypeDefinition(_classDefinition));

        CsTemplateHelper.renderTypeParameters(result, _classDefinition);
        CsTemplateHelper.renderBaseClass(result, _classDefinition);
        CsTemplateHelper.renderImplementedInterfaces(result, _classDefinition);
        CsTemplateHelper.renderTypeParameterConstraints(result, _classDefinition);

        result.newLine();
        result.appendNewLine(TemplateHelper.BLOCK_OPEN);

        CsTemplateHelper.renderConstructors(result, _classDefinition, false);
        CsTemplateHelper.renderFields(result, _classDefinition);
        CsTemplateHelper.renderMethods(result, _classDefinition);

        result.append(TemplateHelper.BLOCK_CLOSE);

        CsTemplateHelper.renderErasedProxyType(result, _classDefinition);

        return result;
    }
}

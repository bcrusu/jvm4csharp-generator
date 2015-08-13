package com.jvm4csharp.generator.csharp;

import com.jvm4csharp.generator.GenerationResult;
import com.jvm4csharp.generator.TemplateHelper;
import com.jvm4csharp.generator.reflectx.XClass;
import com.jvm4csharp.generator.reflectx.XClassDefinition;

public class CsEnumTemplate implements ICsTemplate {
    private final XClassDefinition _classDefinition;

    public CsEnumTemplate(XClassDefinition classDefinition) {
        _classDefinition = classDefinition;
    }

    @Override
    public GenerationResult generate() {
        XClass xClass = _classDefinition.getXClass();
        String internalTypeName = xClass.getInternalTypeName();

        GenerationResult result = new GenerationResult();

        result.append("[JavaProxy(\"");
        result.append(internalTypeName);
        result.appendNewLine("\")]");

        result.append("public");
        if (xClass.isAbstract())
            result.append(" abstract");
        else
            result.append(" sealed");
        result.append(" class ");
        result.append(CsType.renderTypeDefinition(_classDefinition));

        CsTemplateHelper.renderBaseClass(result, _classDefinition);
        CsTemplateHelper.renderImplementedInterfaces(result, _classDefinition);
        CsTemplateHelper.renderTypeParameterConstraints(result, xClass);

        result.newLine();
        result.appendNewLine(TemplateHelper.BLOCK_OPEN);

        CsTemplateHelper.renderConstructors(result, _classDefinition);
        CsTemplateHelper.renderFields(result, _classDefinition);
        CsTemplateHelper.renderMethods(result, _classDefinition);

        result.append(TemplateHelper.BLOCK_CLOSE);

        return result;
    }
}

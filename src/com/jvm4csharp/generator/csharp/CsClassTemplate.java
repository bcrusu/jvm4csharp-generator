package com.jvm4csharp.generator.csharp;

import com.jvm4csharp.generator.GenerationResult;
import com.jvm4csharp.generator.TemplateHelper;
import com.jvm4csharp.generator.reflectx.XClass;
import com.jvm4csharp.generator.reflectx.XClassDefinition;

public class CsClassTemplate implements ICsTemplate {
    private final XClassDefinition _classDefinition;

    public CsClassTemplate(XClassDefinition classDefinition) {
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
        if (xClass.isFinal())
            result.append(" sealed");
        if (addPartialKeyword())
            result.append(" partial");

        result.append(" class ");
        result.append(CsType.renderTypeDefinition(_classDefinition));

        CsTemplateHelper.renderTypeParameters(result, _classDefinition);
        CsTemplateHelper.renderBaseClass(result, _classDefinition);
        CsTemplateHelper.renderImplementedInterfaces(result, _classDefinition);
        CsTemplateHelper.renderTypeParameterConstraints(result, _classDefinition);

        result.newLine();
        result.appendNewLine(TemplateHelper.BLOCK_OPEN);

        CsTemplateHelper.renderConstructors(result, _classDefinition);
        CsTemplateHelper.renderFields(result, _classDefinition);
        CsTemplateHelper.renderMethods(result, _classDefinition);

        result.append(TemplateHelper.BLOCK_CLOSE);

        return result;
    }

    private boolean addPartialKeyword() {
        XClass xClass = _classDefinition.getXClass();
        if (xClass.isClass(Object.class) || xClass.isClass(Throwable.class) ||
                xClass.isClass(Class.class) || xClass.isClass(String.class))
            return true;
        return false;
    }
}

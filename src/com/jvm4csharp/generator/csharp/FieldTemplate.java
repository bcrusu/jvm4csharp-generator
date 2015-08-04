package com.jvm4csharp.generator.csharp;

import com.jvm4csharp.generator.IItemTemplate;
import com.jvm4csharp.generator.ReflectionHelper;
import com.jvm4csharp.generator.TemplateHelper;

import java.lang.reflect.Field;

public class FieldTemplate implements IItemTemplate {
    private final Field _field;

    public FieldTemplate(Field field) {

        _field = field;
    }

    @Override
    public String[] GetDependencies() {
        return new String[0];
    }

    @Override
    public String GeneratePrivate() {
        return null;
    }

    @Override
    public String GenerateBody() {
        boolean isFinal = ReflectionHelper.isFinal(_field);
        boolean isStatic = ReflectionHelper.isStatic(_field);
        String name = _field.getName();

        CsType csType = CsConverter.GetClrType(_field.getType());

        StringBuilder result = new StringBuilder();

        // signature
        result.append("public");

        if (isStatic)
            result.append(" static");

        result.append(TemplateHelper.SPACE);
        result.append(csType.DisplayName);

        result.append(TemplateHelper.SPACE);
        result.append(name);

        result.append(TemplateHelper.NEWLINE);
        result.append(TemplateHelper.BLOCK_OPEN);
        result.append(TemplateHelper.NEWLINE);

        // getter
        result.append(TemplateHelper.TAB);
        result.append("get { ");
        result.append("return Get");
        if (isStatic)
            result.append("Static");
        result.append("Field<");
        result.append(csType.DisplayName);
        result.append(">(\"");
        result.append(name);
        result.append("\", \"");
        result.append("tODO");
        result.append("\"); }");






        return result.toString();
    }
}

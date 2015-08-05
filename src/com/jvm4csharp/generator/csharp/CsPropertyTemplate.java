package com.jvm4csharp.generator.csharp;

import com.jvm4csharp.generator.GenerateResult;
import com.jvm4csharp.generator.ReflectionHelper;
import com.jvm4csharp.generator.TemplateHelper;

import java.lang.reflect.Field;

public class CsPropertyTemplate implements ICsItemTemplate {
    private final Field _field;
    private final CsType _fieldCsType;
    private Class _declaringClass;

    public CsPropertyTemplate(Field field, Class declaringClass) {

        _field = field;
        _fieldCsType = CsConverter.GetClrType(_field.getType());
        _declaringClass = declaringClass;
    }

    @Override
    public GenerateResult generate() {
        boolean isFinal = ReflectionHelper.isFinal(_field);
        boolean isStatic = ReflectionHelper.isStatic(_field);
        String name = _field.getName();

        String internalTypeName = ReflectionHelper.GetInternalTypeName(_field.getType());
        CsType declaringClassCsType = CsConverter.GetClrType(_declaringClass);


        GenerateResult result = new GenerateResult();

        // signature
        result.append("public");

        if (isStatic)
            result.append(" static");

        result.append(TemplateHelper.SPACE);
        result.append(_fieldCsType.displayName);

        result.append(TemplateHelper.SPACE);
        result.appendNewLine(name);
        result.appendNewLine(TemplateHelper.BLOCK_OPEN);


        // getter
        result.append(TemplateHelper.TAB);
        result.append("get { ");
        result.append("return Get");
        if (isStatic)
            result.append("Static");
        result.append("Field<");
        result.append(_fieldCsType.displayName);
        result.append(">(");

        if (isStatic) {
            result.append("typeof(");
            result.append(declaringClassCsType.displayName);
            result.append("), ");
        }

        result.append("\"");
        result.append(name);
        result.append("\", \"");
        result.append(internalTypeName);
        result.appendNewLine("\"); }");

        // setter
        if (!isFinal) {
            result.append(TemplateHelper.TAB);
            result.append("set { ");
            result.append("return Set");
            if (isStatic)
                result.append("Static");
            result.append("Field(");
            if (isStatic) {
                result.append("typeof(");
                result.append(declaringClassCsType.displayName);
                result.append("), ");
            }
            result.append("\"");
            result.append(name);
            result.append("\", \"");
            result.append(internalTypeName);
            result.appendNewLine("\", value); }");
        }

        result.append(TemplateHelper.BLOCK_CLOSE);

        return result;
    }

    public CsType[] getReferencedCsTypes() {
        CsType[] result = new CsType[1];
        result[0] = _fieldCsType;
        return result;
    }
}

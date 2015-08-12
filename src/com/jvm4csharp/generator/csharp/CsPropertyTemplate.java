package com.jvm4csharp.generator.csharp;

import com.jvm4csharp.generator.GenerationResult;
import com.jvm4csharp.generator.TemplateHelper;
import com.jvm4csharp.generator.reflectx.XField;

public class CsPropertyTemplate implements ICsTemplate {
    private final XField _field;

    public CsPropertyTemplate(XField field) {
        _field = field;
    }

    @Override
    public GenerationResult generate() {
        String internalTypeName = _field.getInternalTypeName();
        String declaringClassName = CsType.getSimpleClassName(_field.getDeclaringClass());
        String fieldTypeDisplayName = CsType.getDisplayName(_field.getType());

        GenerationResult result = new GenerationResult();

        // signature
        result.append("[JavaSignature(\"");
        result.append(internalTypeName);
        result.appendNewLine("\")]");

        if (!_field.getDeclaringClass().isInterface()) {
            result.append("public ");
            if (_field.isStatic())
                result.append("static ");
        }

        result.append(fieldTypeDisplayName);
        result.append(TemplateHelper.SPACE);
        result.append(CsTemplateHelper.escapeCsKeyword(_field.getName()));


        if (_field.getDeclaringClass().isInterface()) {
            result.append(" { get; ");
            if (!_field.isFinal())
                result.append("set; ");
            result.append("}");
        } else {
            result.newLine();
            result.appendNewLine(TemplateHelper.BLOCK_OPEN);

            // getter
            result.append(TemplateHelper.TAB);
            result.append("get { return Get");
            if (_field.isStatic())
                result.append("Static");
            result.append("Field<");
            result.append(fieldTypeDisplayName);
            result.append(">(");

            if (_field.isStatic()) {
                result.append("typeof(");
                result.append(declaringClassName);
                result.append("), ");
            }

            result.append("\"");
            result.append(_field.getName());
            result.append("\", \"");
            result.append(internalTypeName);
            result.append("\"); }");

            // setter
            if (!_field.isFinal()) {
                result.newLine();
                result.append(TemplateHelper.TAB);

                result.append("set { Set");
                if (_field.isStatic())
                    result.append("Static");
                result.append("Field(");
                if (_field.isStatic()) {
                    result.append("typeof(");
                    result.append(declaringClassName);
                    result.append("), ");
                }
                result.append("\"");
                result.append(_field.getName());
                result.append("\", \"");
                result.append(internalTypeName);
                result.append("\", value); }");
            }

            result.newLine();
            result.append(TemplateHelper.BLOCK_CLOSE);
        }

        return result;
    }
}

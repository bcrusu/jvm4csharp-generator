package com.jvm4csharp.generator.csharp;

import com.jvm4csharp.generator.ClassDetails;
import com.jvm4csharp.generator.GenerationResult;
import com.jvm4csharp.generator.ReflectionHelper;
import com.jvm4csharp.generator.TemplateHelper;

import java.lang.reflect.Field;

public class CsPropertyTemplate implements ICsTemplate {
    private final Field _field;
    private final CsType _fieldCsType;
    private ClassDetails _declaringClassDetails;

    public CsPropertyTemplate(Field field, ClassDetails declaringClassDetails) {
        _field = field;
        _fieldCsType = CsType.getCsType(_field.getGenericType());
        _declaringClassDetails = declaringClassDetails;
    }

    @Override
    public GenerationResult generate() {
        boolean isFinal = ReflectionHelper.isFinal(_field);
        boolean isStatic = ReflectionHelper.isStatic(_field);

        String fieldName = _field.getName();
        String internalTypeName = ReflectionHelper.getInternalTypeName(_field.getType());
        CsType declaringClassCsType = CsType.getCsType(_declaringClassDetails.Class);

        GenerationResult result = new GenerationResult();

        // signature
        result.append("[JavaSignature(\"");
        result.append(internalTypeName);
        result.appendNewLine("\")]");

        if (!_declaringClassDetails.Class.isInterface()) {
            result.append("public ");
            if (isStatic)
                result.append("static ");
        }

        result.append(_fieldCsType.displayName);
        result.append(TemplateHelper.SPACE);
        result.append(CsTemplateHelper.escapeCsKeyword(fieldName));


        if (_declaringClassDetails.Class.isInterface()) {
            result.append(" { get; ");
            if (!isFinal)
                result.append("set; ");
            result.append("}");
        } else {
            result.newLine();
            result.appendNewLine(TemplateHelper.BLOCK_OPEN);

            // getter
            result.append(TemplateHelper.TAB);
            result.append("get { return Get");
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
            result.append(fieldName);
            result.append("\", \"");
            result.append(internalTypeName);
            result.append("\"); }");

            // setter
            if (!isFinal) {
                result.newLine();
                result.append(TemplateHelper.TAB);

                result.append("set { Set");
                if (isStatic)
                    result.append("Static");
                result.append("Field(");
                if (isStatic) {
                    result.append("typeof(");
                    result.append(declaringClassCsType.displayName);
                    result.append("), ");
                }
                result.append("\"");
                result.append(fieldName);
                result.append("\", \"");
                result.append(internalTypeName);
                result.append("\", value); }");
            }

            result.newLine();
            result.append(TemplateHelper.BLOCK_CLOSE);
        }

        return result;
    }

    public CsType[] getReferencedCsTypes() {
        CsType[] result = new CsType[1];
        result[0] = _fieldCsType;
        return result;
    }
}

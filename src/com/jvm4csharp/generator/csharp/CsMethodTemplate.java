package com.jvm4csharp.generator.csharp;

import com.jvm4csharp.generator.GenerationResult;
import com.jvm4csharp.generator.TemplateHelper;
import com.jvm4csharp.generator.reflectx.XMethod;
import com.jvm4csharp.generator.reflectx.XType;

import java.util.List;

//TODO: use C# 'params' keyword for var args
public class CsMethodTemplate implements ICsTemplate {
    private final XMethod _method;

    public CsMethodTemplate(XMethod method) {
        _method = method;
    }

    @Override
    public GenerationResult generate() {
        String internalSignature = _method.getInternalSignature();
        String returnTypeDisplayName = CsType.getDisplayName(_method.getReturnType());
        List<XType> parameterTypes = _method.getParameterTypes();
        String[] parameterNames = CsTemplateHelper.getEscapedParameterNames(_method);
        String declaringClassName = CsType.getSimpleClassName(_method.getDeclaringClass());

        GenerationResult result = new GenerationResult();

        // signature
        result.append("[JavaSignature(\"");
        result.append(internalSignature);
        result.appendNewLine("\")]");

        if (!_method.getDeclaringClass().isInterface())
            result.append("public ");
        if (_method.isStatic())
            result.append("static ");

        result.append(returnTypeDisplayName);
        result.append(TemplateHelper.SPACE);
        result.append(CsTemplateHelper.escapeCsKeyword(_method.getName()));

        CsTemplateHelper.renderTypeParameters(result, _method);

        result.append('(');
        for (int i = 0; i < parameterNames.length; i++) {
            result.append(CsType.getDisplayName(parameterTypes.get(i)));
            result.append(TemplateHelper.SPACE);
            result.append(parameterNames[i]);

            if (i < parameterNames.length - 1)
                result.append(", ");
        }
        result.append(')');

        // body
        if (_method.getDeclaringClass().isInterface()) {
            result.append(";");
        } else {
            result.newLine();
            result.appendNewLine(TemplateHelper.BLOCK_OPEN);

            result.append(TemplateHelper.TAB);
            if (!_method.isVoidReturnType())
                result.append("return ");

            result.append("Call");
            if (_method.isStatic())
                result.append("Static");
            result.append("Method");

            if (!_method.isVoidReturnType()) {
                result.append("<");
                result.append(returnTypeDisplayName);
                result.append(">");
            }

            result.append('(');
            if (_method.isStatic()) {
                result.append("typeof(");
                result.append(declaringClassName);
                result.append("), ");
            }
            result.append("\"");
            result.append(_method.getName());
            result.append("\", \"");
            result.append(internalSignature);
            result.append("\"");

            for (String csParameterName : parameterNames) {
                result.append(", ");
                result.append(csParameterName);
            }

            result.appendNewLine(");");
            result.append(TemplateHelper.BLOCK_CLOSE);
        }

        return result;
    }
}

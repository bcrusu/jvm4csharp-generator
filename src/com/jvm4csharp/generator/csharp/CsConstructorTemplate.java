package com.jvm4csharp.generator.csharp;

import com.jvm4csharp.generator.GenerationResult;
import com.jvm4csharp.generator.TemplateHelper;
import com.jvm4csharp.generator.reflectx.XClass;
import com.jvm4csharp.generator.reflectx.XConstructor;
import com.jvm4csharp.generator.reflectx.XType;

import java.util.List;

public class CsConstructorTemplate implements ICsTemplate {
    private final XConstructor _constructor;

    public CsConstructorTemplate(XConstructor constructor){
        _constructor = constructor;
    }

    @Override
    public GenerationResult generate() {
        String internalSignature = _constructor.getInternalSignature();
        List<XType> parameterTypes = _constructor.getParameterTypes();
        String[] parameterNames = CsTemplateHelper.getEscapedParameterNames(_constructor);
        XClass declaringClass = _constructor.getDeclaringClass();

        GenerationResult result = new GenerationResult();

        // signature
        result.append("public");
        result.append(TemplateHelper.SPACE);
        result.append(CsType.getSimpleClassName(declaringClass));

        result.append('(');
        for (int i = 0; i < parameterNames.length; i++) {
            result.append(CsType.getDisplayName(parameterTypes.get(i)));
            result.append(TemplateHelper.SPACE);
            result.append(parameterNames[i]);

            if (i < parameterNames.length - 1)
                result.append(", ");
        }
        result.append(")");
        if (!declaringClass.isClass(Object.class) && !declaringClass.isClass(Throwable.class))
            result.append(" : base(JavaVoid.Void)");

        result.newLine();
        result.appendNewLine(TemplateHelper.BLOCK_OPEN);

        // body
        result.append(TemplateHelper.TAB);
        result.append("CallConstructor(\"");
        result.append(internalSignature);
        result.append("\"");

        for (String csParameterName : parameterNames) {
            result.append(", ");
            result.append(csParameterName);
        }

        result.appendNewLine(");");
        result.append(TemplateHelper.BLOCK_CLOSE);

        return result;
    }
}

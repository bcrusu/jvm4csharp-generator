package com.jvm4csharp.generator.csharp;

import com.jvm4csharp.generator.GenerateResult;
import com.jvm4csharp.generator.IItemTemplate;
import com.jvm4csharp.generator.ReflectionHelper;
import com.jvm4csharp.generator.TemplateHelper;

import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;

public class CsConstructorTemplate implements ICsItemTemplate {
    private final Constructor _constructor;
    private final Class _declaringClass;
    private final CsType[] _parametersCsTypes;

    public CsConstructorTemplate(Constructor constructor, Class declaringClass){
        _constructor = constructor;
        _declaringClass = declaringClass;

        Class[] parameterTypes = constructor.getParameterTypes();
        _parametersCsTypes = new CsType[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            _parametersCsTypes[i] = CsConverter.GetClrType(parameterTypes[i]);
        }
    }

    @Override
    public GenerateResult generate() {
        String internalSignature = ReflectionHelper.GetInternalSignature(_constructor);
        CsType declaringClassCsType = CsConverter.GetClrType(_declaringClass);
        Parameter[] parameters = _constructor.getParameters();

        GenerateResult result = new GenerateResult();

        // signature
        result.append("public");
        result.append(TemplateHelper.SPACE);
        result.append(_declaringClass.getSimpleName());

        result.append('(');
        for (int i = 0; i < parameters.length; i++) {
            result.append(_parametersCsTypes[i].displayName);
            result.append(TemplateHelper.SPACE);
            result.append(parameters[i].getName());

            if (i < parameters.length - 2)
                result.append(',');
        }
        result.appendNewLine(") : base(JavaVoid.Void)");
        result.appendNewLine(TemplateHelper.BLOCK_OPEN);

        // body
        result.append(TemplateHelper.TAB);
        result.append("CallConstructor(\"");
        result.append(internalSignature);
        result.append("\"");

        for (int i = 0; i < parameters.length; i++) {
            result.append(", ");
            result.append(parameters[i].getName());
        }

        result.appendNewLine(");");
        result.append(TemplateHelper.BLOCK_CLOSE);

        return result;
    }

    @Override
    public CsType[] getReferencedCsTypes() {
        return _parametersCsTypes;
    }
}

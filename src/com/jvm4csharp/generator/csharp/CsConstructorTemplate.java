package com.jvm4csharp.generator.csharp;

import com.jvm4csharp.generator.GenerationResult;
import com.jvm4csharp.generator.ReflectionHelper;
import com.jvm4csharp.generator.TemplateHelper;

import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;

//TODO: render outer class "this" parameter for inner classes
public class CsConstructorTemplate implements ICsTemplate {
    private final Constructor _constructor;
    private final Class _declaringClass;
    private final CsType[] _parametersCsTypes;

    public CsConstructorTemplate(Constructor constructor, Class declaringClass){
        _constructor = constructor;
        _declaringClass = declaringClass;

        Type[] parameterTypes = constructor.getGenericParameterTypes();
        _parametersCsTypes = new CsType[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            _parametersCsTypes[i] = CsType.getCsType(parameterTypes[i]);
        }
    }

    @Override
    public GenerationResult generate() {
        String internalSignature = ReflectionHelper.getInternalSignature(_constructor);
        Parameter[] parameters = _constructor.getParameters();

        GenerationResult result = new GenerationResult();

        // signature
        result.append("public");
        result.append(TemplateHelper.SPACE);
        result.append(CsType.getCsClassName(_declaringClass));

        result.append('(');
        for (int i = 0; i < parameters.length; i++) {
            result.append(_parametersCsTypes[i].displayName);
            result.append(TemplateHelper.SPACE);
            result.append(parameters[i].getName());

            if (i < parameters.length - 1)
                result.append(", ");
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

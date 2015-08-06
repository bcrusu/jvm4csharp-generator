package com.jvm4csharp.generator.csharp;

import com.jvm4csharp.generator.GenerateResult;
import com.jvm4csharp.generator.ReflectionHelper;
import com.jvm4csharp.generator.TemplateHelper;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

//TODO: generics, var args
public class CsMethodTemplate implements ICsTemplate {
    private final Method _method;
    private final Class _declaringClass;
    private final CsType _returnCsType;
    private final CsType[] _parametersCsTypes;

    public CsMethodTemplate(Method method, Class declaringClass) {
        _method = method;
        _declaringClass = declaringClass;
        _returnCsType = CsConverter.GetCsType(method.getReturnType());

        Class[] parameterTypes = method.getParameterTypes();
        _parametersCsTypes = new CsType[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            _parametersCsTypes[i] = CsConverter.GetCsType(parameterTypes[i]);
        }
    }

    @Override
    public GenerateResult[] generate() {
        //TODO: boolean isFinal = ReflectionHelper.isFinal(_method);
        boolean isStatic = ReflectionHelper.isStatic(_method);

        String name = _method.getName();
        String internalSignature = ReflectionHelper.GetInternalSignature(_method);
        CsType declaringClassCsType = CsConverter.GetCsType(_declaringClass);
        Parameter[] parameters = _method.getParameters();
        Class returnType = _method.getReturnType();

        GenerateResult result = new GenerateResult();

        // signature
        result.append("public");

        if (isStatic)
            result.append(" static");

        result.append(TemplateHelper.SPACE);
        result.append(_returnCsType.displayName);

        result.append(TemplateHelper.SPACE);
        result.append(name);

        result.append('(');
        for (int i = 0; i < parameters.length; i++) {
            result.append(_parametersCsTypes[i].displayName);
            result.append(TemplateHelper.SPACE);
            result.append(parameters[i].getName());

            if (i < parameters.length - 2)
                result.append(',');
        }
        result.appendNewLine(')');
        result.appendNewLine(TemplateHelper.BLOCK_OPEN);

        // body
        result.append(TemplateHelper.TAB);
        if (returnType != Void.TYPE)
            result.append("return ");

        result.append("Call");
        if (isStatic)
            result.append("Static");
        result.append("Method");

        if (returnType != Void.TYPE) {
            result.append("<");
            result.append(_returnCsType.displayName);
            result.append(">");
        }

        result.append('(');
        if (isStatic) {
            result.append("typeof(");
            result.append(declaringClassCsType.displayName);
            result.append("), ");
        }
        result.append("\"");
        result.append(name);
        result.append("\", \"");
        result.append(internalSignature);
        result.append("\"");

        for (int i = 0; i < parameters.length; i++) {
            result.append(", ");
            result.append(parameters[i].getName());
        }

        result.appendNewLine(");");
        result.append(TemplateHelper.BLOCK_CLOSE);

        GenerateResult[] results = new GenerateResult[1];
        results[0] = result;
        return results;
    }

    @Override
    public CsType[] getReferencedCsTypes() {
        CsType[] result = new CsType[_parametersCsTypes.length + 1];
        result[0] = _returnCsType;
        for (int i = 0; i < _parametersCsTypes.length; i++) {
            result[i + 1] = _parametersCsTypes[i];
        }
        return result;
    }
}

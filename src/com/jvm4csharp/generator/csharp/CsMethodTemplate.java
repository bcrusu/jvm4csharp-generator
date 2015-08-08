package com.jvm4csharp.generator.csharp;

import com.jvm4csharp.generator.GenerationResult;
import com.jvm4csharp.generator.ReflectionHelper;
import com.jvm4csharp.generator.TemplateHelper;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;

//TODO: var args
public class CsMethodTemplate implements ICsTemplate {
    private final Method _method;
    private final Class _declaringClass;
    private final CsType _returnCsType;
    private final CsType[] _parametersCsTypes;

    public CsMethodTemplate(Method method, Class declaringClass) {
        _method = method;
        _declaringClass = declaringClass;
        _returnCsType = CsType.getCsType(method.getGenericReturnType());

        Type[] parameterTypes = method.getGenericParameterTypes();
        _parametersCsTypes = new CsType[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            _parametersCsTypes[i] = CsType.getCsType(parameterTypes[i]);
        }
    }

    @Override
    public GenerationResult generate() {
        boolean isStatic = ReflectionHelper.isStatic(_method);

        String methodName = _method.getName();
        String internalSignature = ReflectionHelper.getInternalSignature(_method);
        CsType declaringClassCsType = CsType.getCsType(_declaringClass);
        Class returnType = _method.getReturnType();
        String[] csParameterNames = CsTemplateHelper.getCsParameterNames(_method);

        GenerationResult result = new GenerationResult();

        // signature
        result.append("[JavaSignature(\"");
        result.append(internalSignature);
        result.appendNewLine("\")]");

        if (!_declaringClass.isInterface())
            result.append("public ");
        if (isStatic)
            result.append("static ");

        result.append(_returnCsType.displayName);
        result.append(TemplateHelper.SPACE);
        result.append(CsTemplateHelper.escapeCsKeyword(methodName));

        CsTemplateHelper.renderTypeParameters(result, _method);

        result.append('(');
        for (int i = 0; i < csParameterNames.length; i++) {
            result.append(_parametersCsTypes[i].displayName);
            result.append(TemplateHelper.SPACE);
            result.append(csParameterNames[i]);

            if (i < csParameterNames.length - 1)
                result.append(", ");
        }
        result.append(')');

        // body
        if (_declaringClass.isInterface()) {
            result.append(";");
        } else {
            result.newLine();
            result.appendNewLine(TemplateHelper.BLOCK_OPEN);

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
            result.append(methodName);
            result.append("\", \"");
            result.append(internalSignature);
            result.append("\"");

            for (String csParameterName : csParameterNames) {
                result.append(", ");
                result.append(csParameterName);
            }

            result.appendNewLine(");");
            result.append(TemplateHelper.BLOCK_CLOSE);
        }

        return result;
    }

    @Override
    public CsType[] getReferencedCsTypes() {
        ArrayList<CsType> result = new ArrayList<>();
        result.add(_returnCsType);
        result.addAll(Arrays.asList(_parametersCsTypes));
        return result.toArray(new CsType[result.size()]);
    }
}

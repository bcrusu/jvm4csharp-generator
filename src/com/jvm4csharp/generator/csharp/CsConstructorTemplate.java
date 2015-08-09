package com.jvm4csharp.generator.csharp;

import com.jvm4csharp.generator.ClassDetails;
import com.jvm4csharp.generator.GenerationResult;
import com.jvm4csharp.generator.ReflectionHelper;
import com.jvm4csharp.generator.TemplateHelper;

import java.lang.reflect.Constructor;
import java.lang.reflect.Type;

public class CsConstructorTemplate implements ICsTemplate {
    private final Constructor _constructor;
    private final ClassDetails _declaringClassDetails;
    private final CsType[] _parametersCsTypes;

    public CsConstructorTemplate(Constructor constructor, ClassDetails declaringClassDetails){
        _constructor = constructor;
        _declaringClassDetails = declaringClassDetails;

        Type[] parameterTypes = constructor.getGenericParameterTypes();
        _parametersCsTypes = new CsType[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            _parametersCsTypes[i] = CsType.getCsType(parameterTypes[i]);
        }
    }

    @Override
    public GenerationResult generate() {
        String internalSignature = ReflectionHelper.getInternalSignature(_constructor);
        String[] csParameterNames = CsTemplateHelper.getCsParameterNames(_constructor);

        GenerationResult result = new GenerationResult();

        // signature
        result.append("public");
        result.append(TemplateHelper.SPACE);
        result.append(CsType.getCsClassName(_declaringClassDetails.Class));

        result.append('(');
        for (int i = 0; i < csParameterNames.length; i++) {
            result.append(_parametersCsTypes[i].displayName);
            result.append(TemplateHelper.SPACE);
            result.append(csParameterNames[i]);

            if (i < csParameterNames.length - 1)
                result.append(", ");
        }
        result.append(")");
        if (_declaringClassDetails.Class != Object.class && _declaringClassDetails.Class != Throwable.class)
            result.append(" : base(JavaVoid.Void)");

        result.newLine();
        result.appendNewLine(TemplateHelper.BLOCK_OPEN);

        // body
        result.append(TemplateHelper.TAB);
        result.append("CallConstructor(\"");
        result.append(internalSignature);
        result.append("\"");

        for (String csParameterName : csParameterNames) {
            result.append(", ");
            result.append(csParameterName);
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

package com.jvm4csharp.generator.csharp;

import com.jvm4csharp.generator.IItemTemplate;
import com.jvm4csharp.generator.ReflectionHelper;

import java.util.List;
import java.util.stream.Collectors;

public class ClassTemplate implements IItemTemplate {
    private final Class _class;

    public ClassTemplate(Class clazz){
        _class = clazz;
    }

    @Override
    public String[] GetDependencies() {
        return new String[0];
    }

    @Override
    public String GeneratePrivate() {
        return null;
    }

    @Override
    public String GenerateBody() {
        List<FieldTemplate> fields = ReflectionHelper.getPublicDeclaredFields(_class)
                .stream()
                .map(x -> new FieldTemplate(x))
                .collect(Collectors.toList());

        List<MethodTemplate> methods = ReflectionHelper.getPublicDeclaredMethods(_class)
                .stream()
                .map(x -> new MethodTemplate(x))
                .collect(Collectors.toList());

        List<ConstructorTemplate> constructors = ReflectionHelper.getPublicDeclaredConstructors(_class)
                .stream()
                .map(x -> new ConstructorTemplate(x))
                .collect(Collectors.toList());

        List<ClassTemplate> classes = ReflectionHelper.getPublicDeclaredClasses(_class)
                .stream()
                .map(x -> new ClassTemplate(x))
                .collect(Collectors.toList());


        StringBuilder result = new StringBuilder();

        for(FieldTemplate field : fields){
            String template = field.GenerateBody();
        }

        return result.toString();
    }
}

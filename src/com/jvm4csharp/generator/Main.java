package com.jvm4csharp.generator;

import com.jvm4csharp.generator.csharp.ClassTemplate;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import java.lang.reflect.Modifier;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class Main {
    public static void main(String[] args) {
        Set<String> allTypes;

        try {
            Reflections reflections = GetReflections();

            allTypes = reflections.getAllTypes();

            for(String typeName : allTypes){
                if (!typeName.startsWith("java.lang."))
                    continue;

                Class clazz = Class.forName(typeName);
                if (!ReflectionHelper.isPublic(clazz))
                    continue;

                if (clazz.getDeclaringClass() != null)
                    continue;

                if (clazz.getFields().length == 0)
                    continue;

                ClassTemplate template = new ClassTemplate(clazz);
                String str = template.GenerateBody();
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    private static Reflections GetReflections() {
        List<ClassLoader> classLoadersList = new LinkedList<ClassLoader>();
        classLoadersList.add(ClasspathHelper.contextClassLoader());
        classLoadersList.add(ClasspathHelper.staticClassLoader());

        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .setScanners(new SubTypesScanner(false /* don't exclude Object.class */), new ResourcesScanner())
                .setUrls(ClasspathHelper.forJavaClassPath()));

        return reflections;
    }

    private static boolean GetShouldGenerateClass(Class clazz){
        int mod = clazz.getModifiers();
        if (!Modifier.isPublic(mod))
            return false;

        return true;
    }
}

package com.jvm4csharp.generator;

import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import java.lang.Class;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class ClassSelector {
    public LinkedList<Class> getClasses(String pattern) {
        Reflections reflections = GetReflections();
        Set<String> allTypes = reflections.getAllTypes();

        LinkedList<Class> result = new LinkedList<>();

        for (String typeName : allTypes) {
            //TODO: use regex
            if (!typeName.startsWith(pattern))
                continue;

            try {
                Class clazz = Class.forName(typeName);
                result.add(clazz);
            } catch (ClassNotFoundException e) {
                System.out.format("Could not load class: %1s", typeName);
            }
        }

        return result;
    }

    //TODO: better class scanner - ignore non-public
    private static Reflections GetReflections() {
        List<ClassLoader> classLoadersList = new LinkedList<ClassLoader>();
        classLoadersList.add(ClasspathHelper.contextClassLoader());
        classLoadersList.add(ClasspathHelper.staticClassLoader());

        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .setScanners(new SubTypesScanner(false /* don't exclude Object.class */), new ResourcesScanner())
                .setUrls(ClasspathHelper.forJavaClassPath()));

        return reflections;
    }
}

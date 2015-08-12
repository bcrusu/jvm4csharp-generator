package com.jvm4csharp.generator;

import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import java.lang.Class;
import java.lang.reflect.Modifier;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class ClassSelector {
    private final String[] _includePatterns;

    public ClassSelector(String[] includePatterns) {
        _includePatterns = includePatterns;
    }

    public LinkedList<Class> getClasses() {
        Reflections reflections = GetReflections();
        Set<String> allTypes = reflections.getAllTypes();

        LinkedList<Class> result = new LinkedList<>();

        for (String typeName : allTypes) {
            if (!isTypeIncluded(typeName))
                continue;

            try {
                Class clazz = Class.forName(typeName);
                if (canGenerateClass(clazz))
                    result.add(clazz);
            } catch (ClassNotFoundException e) {
                System.out.format("Could not load class: %1s", typeName);
            }
        }

        return result;
    }

    //TODO: better class scanner - ignore non-public classes
    private static Reflections GetReflections() {
        List<ClassLoader> classLoadersList = new LinkedList<ClassLoader>();
        classLoadersList.add(ClasspathHelper.contextClassLoader());
        classLoadersList.add(ClasspathHelper.staticClassLoader());

        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .setScanners(new SubTypesScanner(false /* don't exclude Object.class */), new ResourcesScanner())
                .setUrls(ClasspathHelper.forJavaClassPath()));

        return reflections;
    }

    private boolean isTypeIncluded(String typeName) {
        for (String pattern : _includePatterns)
            if (typeName.startsWith(pattern))   //TODO: use regex
                return true;

        return false;
    }

    private boolean canGenerateClass(Class clazz) {
        boolean isPublic = Modifier.isPublic(clazz.getModifiers());

        return isPublic && !clazz.isPrimitive() && !clazz.isArray() &&
                !clazz.isSynthetic() && !clazz.isLocalClass() && !clazz.isAnonymousClass();
    }
}

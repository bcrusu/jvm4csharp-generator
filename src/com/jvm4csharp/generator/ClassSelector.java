package com.jvm4csharp.generator;

import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.*;

public class ClassSelector {
    private final String[] _includePackages;

    public ClassSelector(String[] includePackages) {
        _includePackages = includePackages;
    }

    public LinkedList<Class> getClasses() {
        Reflections reflections = GetReflections();
        Set<String> allTypes = reflections.getAllTypes();

        LinkedList<Class> result = new LinkedList<>();

        for (String typeName : allTypes) {
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

    private Reflections GetReflections() {
        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .filterInputsBy(getFilterBuilder())
                .setScanners(new SubTypesScanner(false))
                .setUrls(getJavaClassLibraryJars()));

        return reflections;
    }

    private boolean canGenerateClass(Class clazz) {
        boolean isPublic = Modifier.isPublic(clazz.getModifiers());

        return isPublic && !clazz.isPrimitive() && !clazz.isArray() &&
                !clazz.isSynthetic() && !clazz.isLocalClass() && !clazz.isAnonymousClass();
    }

    private static Collection<URL> getJavaClassLibraryJars() {
        List<URL> result = new ArrayList<>();
        Class clazz = Object.class;
        URL location = clazz.getResource('/' + clazz.getName().replace('.', '/') + ".class");

        result.add(location);
        return result;
    }

    private FilterBuilder getFilterBuilder(){
        FilterBuilder result = new FilterBuilder();

        for (String item : _includePackages)
            result.includePackage(item);

        return result;
    }
}

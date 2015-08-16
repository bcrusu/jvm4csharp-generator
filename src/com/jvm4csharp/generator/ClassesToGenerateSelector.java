package com.jvm4csharp.generator;

import com.jvm4csharp.generator.reflectx.XClassDefinition;
import com.jvm4csharp.generator.reflectx.XClassDefinitionFactory;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.*;

class ClassesToGenerateSelector implements Iterable<XClassDefinition> {
    private final String[] _includedPackages;
    private final boolean _skipOtherPackageReferences;

    public ClassesToGenerateSelector(String[] includedPackages, boolean skipOtherPackageReferences) {
        _includedPackages = includedPackages;
        _skipOtherPackageReferences = skipOtherPackageReferences;
    }

    @Override
    public Iterator<XClassDefinition> iterator() {
        Reflections reflections = getReflections();
        Set<String> allTypes = reflections.getAllTypes();
        String[] allTypesArray = allTypes.toArray(new String[allTypes.size()]);

        return new AllTypesIterator(allTypesArray);
    }

    private Reflections getReflections() {
        return new Reflections(new ConfigurationBuilder()
                .filterInputsBy(getFilterBuilder())
                .setScanners(new SubTypesScanner(false))
                .setUrls(getJavaClassLibraryJars()));
    }

    private static Collection<URL> getJavaClassLibraryJars() {
        List<URL> result = new ArrayList<>();
        Class clazz = Object.class;
        URL location = clazz.getResource('/' + clazz.getName().replace('.', '/') + ".class");

        result.add(location);
        return result;
    }

    private FilterBuilder getFilterBuilder() {
        FilterBuilder result = new FilterBuilder();

        for (String item : _includedPackages)
            result.includePackage(item);

        return result;
    }

    private class AllTypesIterator implements Iterator<XClassDefinition> {
        private final String[] _allTypes;
        private final int _allTypesLength;
        private int _currentIndex;
        private XClassDefinition _next;

        public AllTypesIterator(String[] allTypes) {
            _allTypes = allTypes;
            _allTypesLength = allTypes.length;
        }

        @Override
        public boolean hasNext() {
            while (_currentIndex < _allTypesLength) {
                String typeName = _allTypes[_currentIndex++];

                try {
                    Class clazz = Class.forName(typeName);
                    if (!canGenerate(clazz))
                        continue;

                    System.out.println(String.format("Creating class definition for class: '%1s'.", clazz.getName()));

                    XClassDefinition classDefinition = XClassDefinitionFactory.createClassDefinition(clazz);
                    if (_skipOtherPackageReferences &&
                            Utils.hasOtherPackageReferences(_includedPackages, classDefinition.getReferencedPackagesInDefinition())) {
                        System.out.println(String.format("\tClass '%1s' was excluded from generation.", clazz.getName()));
                        continue;
                    }

                    _next = classDefinition;
                    return true;
                } catch (ClassNotFoundException e) {
                    System.out.format("Could not load class: %1s", typeName);
                }
            }

            return false;
        }

        @Override
        public XClassDefinition next() {
            return _next;
        }

        private boolean canGenerate(Class clazz) {
            boolean isPublic = Modifier.isPublic(clazz.getModifiers());
            if (!isPublic)
                return false;

            Class declaringClass = clazz.getDeclaringClass();
            if (declaringClass != null)
                return false;

            return !clazz.isPrimitive() && !clazz.isArray() && !clazz.isSynthetic() && !clazz.isLocalClass() && !clazz.isAnonymousClass();
        }
    }
}

package com.jvm4csharp.generator;

import com.jvm4csharp.generator.csharp.CsProxyGenerator;
import com.jvm4csharp.generator.reflectx.XClass;
import com.jvm4csharp.generator.reflectx.XClassDefinition;
import com.jvm4csharp.generator.reflectx.XClassDefinitionFactory;

import java.lang.reflect.Modifier;
import java.net.JarURLConnection;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.Spliterator;

//TODO: get parameter names from javadoc
public class Main {
    private static String _outputDirectory;
    private static String _namespacePrefix;
    private static String[] _includedPackages;
    private static boolean _skipOtherPackageReferences;

    public static void main(String[] args) {
        if (!ParseArgs(args)) {
            System.out.println("Invalid command line arguments.");
            System.exit(-1);
        }

        OutputWriter outputWriter = new OutputWriter(_outputDirectory);
        if (!outputWriter.isValidOutputDirectory()) {
            System.out.format("Invalid output path.");
            System.exit(-1);
        }

        ClassesToGenerateSelector classesToGenerateSelector = new ClassesToGenerateSelector(_includedPackages, _skipOtherPackageReferences);
        IProxyGenerator generator = getProxyGenerator();

        for (XClassDefinition classDefinition : classesToGenerateSelector) {
            GenerationResult generationResult = generator.generate(classDefinition);
            outputWriter.write(generationResult);

            System.out.println();
        }

        System.out.println("Done.");
    }

    //TODO: proper command line arguments parser
    private static boolean ParseArgs(String[] args) {
        _outputDirectory = "E:\\work\\github\\jvm4csharp\\jvm4csharp\\generated";
        _namespacePrefix = "jvm4csharp";
        _includedPackages = new String[]{"java.lang", "java.util", "java.math", "java.io", "java.nio", "java.net", "java.text"};
        _skipOtherPackageReferences = true;
        return true;
    }

    private static IProxyGenerator getProxyGenerator() {
        return new CsProxyGenerator(_namespacePrefix);
    }
}

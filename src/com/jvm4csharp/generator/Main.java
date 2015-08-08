package com.jvm4csharp.generator;

import com.jvm4csharp.generator.csharp.CsProxyGenerator;

import java.util.LinkedList;

//TODO: get parameter names from javadoc
public class Main {
    private static String _outputDirectory;
    private static String _namespacePrefix;
    private static String[] _includePatterns;

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

        ClassSelector classSelector = new ClassSelector(_includePatterns);

        LinkedList<Class> classesToGenerate = classSelector.getClasses();

        IProxyGenerator generator = getProxyGenerator();

        for (Class clazz : classesToGenerate) {
            if (!generator.canGenerate(clazz))
                continue;

            System.out.format("Generating class: %1s", clazz.getName());

            GenerationResult generationResult = generator.generate(clazz);

            outputWriter.write(generationResult);

            System.out.println();
        }

        System.out.println("Done.");
    }

    //TODO: proper command line arguments parser
    private static boolean ParseArgs(String[] args) {
        _outputDirectory = "E:\\work\\github\\jvm4csharp\\jvm4csharp\\generated";
        _namespacePrefix = "jvm4csharp";
        _includePatterns = new String[]{"java.lang", "java.util", "java.math", "java.io", "java.nio", "java.net", "java.text", "java.security", "java.time"};
        return true;
    }

    private static IProxyGenerator getProxyGenerator() {
        return new CsProxyGenerator(_namespacePrefix);
    }
}

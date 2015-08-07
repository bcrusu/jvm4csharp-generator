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

            GenerateResult generateResult = generator.generate(clazz);

            outputWriter.write(generateResult);

            System.out.println();
        }

        System.out.println("Done.");
    }

    //TODO: proper command line arguments parser
    private static boolean ParseArgs(String[] args) {
        _outputDirectory = "C:\\work\\gen_out";
        _namespacePrefix = "jvm4csharp";

        String[] includePatterns = {"java.lang", "java.util", "java.math", "java.io", "java.nio", "java.net", "java.text"};
        _includePatterns = includePatterns;
        return true;
    }

    private static IProxyGenerator getProxyGenerator() {
        return new CsProxyGenerator(_namespacePrefix);
    }
}

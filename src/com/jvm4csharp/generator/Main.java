package com.jvm4csharp.generator;

import com.jvm4csharp.generator.csharp.CsProxyGenerator;

import java.util.LinkedList;

public class Main {
    public static void main(String[] args) {
        String outputDirectory = "C:\\work\\gen_out";

        OutputWriter outputWriter = new OutputWriter(outputDirectory);
        if (!outputWriter.isValidOutputDirectory()) {
            System.out.format("Invalid output path.");
            System.exit(-1);
        }

        ClassSelector classSelector = new ClassSelector();
        LinkedList<Class> classesToGenerate = classSelector.getClasses("java.lang");

        IProxyGenerator generator = getProxyGenerator();

        for (Class clazz : classesToGenerate) {
            if (!generator.canGenerate(clazz))
                continue;

            System.out.format("Generating class: %1s", clazz.getName());

            GenerateResult[] generateResults = generator.generate(clazz);

            outputWriter.write(generateResults);
        }

        System.out.format("Done.");
    }

    private static IProxyGenerator getProxyGenerator() {
        return new CsProxyGenerator();
    }
}

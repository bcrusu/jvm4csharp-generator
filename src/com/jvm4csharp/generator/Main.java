package com.jvm4csharp.generator;

import java.util.LinkedList;

public class Main {
    public static void main(String[] args) {
        String outputDirectory = "E:\\gen_out";

        OutputWriter outputWriter = new OutputWriter(outputDirectory);
        if (!outputWriter.isValidOutputDirectory()) {
            System.out.format("Invalid output path.");
            System.exit(-1);
        }

        ClassSelector classSelector = new ClassSelector();
        LinkedList<Class> classesToGenerate = classSelector.getClasses("java.lang");

        for (Class clazz : classesToGenerate) {
            System.out.format("Generating class: %1s", clazz.getName());
            outputWriter.ensurePackagePathExists(clazz.getPackage());

            System.out.println();
        }

        System.out.format("Done.");
    }
}

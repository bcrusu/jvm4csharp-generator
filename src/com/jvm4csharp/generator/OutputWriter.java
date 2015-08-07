package com.jvm4csharp.generator;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

public class OutputWriter {
    private final String _outputPath;
    private final Set<String> _created;
    private final String _fileHeader;

    public OutputWriter(String outputPath) {
        _outputPath = outputPath;
        _created = new HashSet<>();
        _fileHeader = GetFileHeader();
    }

    public boolean isValidOutputDirectory() {
        File file = new File(_outputPath);
        if (!file.exists())
            return false;
        if (!file.isDirectory())
            return false;

        if (file.listFiles().length > 0)
            return false;

        return true;
    }

    public void write(GenerateResult generateResult) {
        ensurePackagePathExists(generateResult.getPath());
        Path outputFilePath = Paths.get(_outputPath, generateResult.getPath(), generateResult.getName());

        File file = outputFilePath.toFile();
        if (file.exists()) {
            System.out.format("File '%1s' already exists.", outputFilePath);
            System.exit(-1);
        }

        try {
            file.createNewFile();

            try (FileOutputStream fos = new FileOutputStream(file);
                 OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF8");
                 BufferedWriter bw = new BufferedWriter(osw)) {
                bw.write(_fileHeader);
                bw.write(generateResult.toString());
            }
        } catch (UnsupportedEncodingException e) {
            System.out.println("Could not find the UTF8 encoding. ");
            System.out.println(e);
            System.exit(-1);
        } catch (FileNotFoundException e) {
            System.out.print("BOOM! where is my file? ");
            System.out.println(e);
            System.exit(-1);
        } catch (IOException e) {
            System.out.print("Could not create output file: ");
            System.out.println(e);
            System.exit(-1);
        }
    }

    private void ensurePackagePathExists(String path) {
        String[] splits = path.split("\\\\");

        String currentPath = _outputPath;
        for (String split : splits) {
            currentPath = Paths.get(currentPath, split).toString();
            if (_created.contains(currentPath))
                continue;

            File file = new File(currentPath);
            file.mkdir();
            _created.add(currentPath);
        }
    }

    private String GetFileHeader() {
        String java_version = System.getProperty("java.version");
        String java_vm_specification_version = System.getProperty("java.vm.specification.version");
        String java_vm_version = System.getProperty("java.vm.version");
        String java_vm_name = System.getProperty("java.vm.name");

        String template = "//------------------------------------------------------------------------" + TemplateHelper.NEWLINE +
                "//\tThis code was generated using jvm4csharp-generator:" + TemplateHelper.NEWLINE +
                "//\thttps://github.com/bcrusu/jvm4csharp-generator" + TemplateHelper.NEWLINE +
                "//" + TemplateHelper.NEWLINE +
                "//\tjava_version\t\t\t\t\t: %1s" + TemplateHelper.NEWLINE +
                "//\tjava_vm_specification_version\t: %2s" + TemplateHelper.NEWLINE +
                "//\tjava_vm_version\t\t\t\t\t: %3s" + TemplateHelper.NEWLINE +
                "//\tjava_vm_name\t\t\t\t\t: %4s" + TemplateHelper.NEWLINE +
                "//------------------------------------------------------------------------" + TemplateHelper.NEWLINE +
                "" + TemplateHelper.NEWLINE;

        return String.format(template,
                java_version,
                java_vm_specification_version,
                java_vm_version,
                java_vm_name);
    }
}

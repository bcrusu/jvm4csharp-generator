package com.jvm4csharp.generator;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

public class OutputWriter {
    private final String _outputPath;
    private final Set<String> _created;

    public OutputWriter(String outputPath) {
        _outputPath = outputPath;
        _created = new HashSet<>();
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

        try {
            File file = outputFilePath.toFile();
            file.createNewFile();

            try (FileOutputStream fos = new FileOutputStream(file);
                 OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF8");
                 BufferedWriter bw = new BufferedWriter(osw)) {
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
}

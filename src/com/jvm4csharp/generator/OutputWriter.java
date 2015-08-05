package com.jvm4csharp.generator;

import java.io.File;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

public class OutputWriter {
    private final String _outputPath;
    private final Set<String> _created;

    public OutputWriter(String outputPath){
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

    public void ensurePackagePathExists(Package pack) {
        String packageName = pack.getName();
        String[] splits = packageName.split("\\.");

        String currentPath = _outputPath;
        for (String split : splits){
            currentPath = Paths.get(currentPath, split).toString();
            if (_created.contains(currentPath))
                continue;

            File file = new File(currentPath);
            file.mkdir();
            _created.add(currentPath);
        }
    }
}

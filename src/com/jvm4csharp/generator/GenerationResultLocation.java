package com.jvm4csharp.generator;

public class GenerationResultLocation {
    private String _name;
    private String _path;

    public GenerationResultLocation(String path, String name) {
        _path = path;
        _name = name;
    }

    public String getName(){
        return _name;
    }

    public String getPath(){
        return _path;
    }
}

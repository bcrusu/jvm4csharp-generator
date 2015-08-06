package com.jvm4csharp.generator;

import java.util.LinkedList;

public class GenerateResult {
    private final LinkedList<String> _lines;
    private StringBuilder _currentLine;
    private String _name;
    private String _path;

    public GenerateResult() {
        _lines = new LinkedList<>();
        _currentLine = new StringBuilder();
    }

    public void newLine() {
        _lines.add(_currentLine.toString());
        _currentLine = new StringBuilder();
    }

    public void clearCurrentLine(){
        _currentLine = new StringBuilder();
    }

    public void append(String str) {
        _currentLine.append(str);
    }

    public void append(char c) {
        _currentLine.append(c);
    }

    public void appendNewLine(String str) {
        _currentLine.append(str);
        newLine();
    }

    public void appendNewLine(char c) {
        _currentLine.append(c);
        newLine();
    }

    public void renderTo(GenerateResult gr, int indentationLevel) {
        String indentation = TemplateHelper.getIndentation(indentationLevel);
        for (int i = 0; i < _lines.size(); i++) {
            String line = _lines.get(i);
            if (indentationLevel > 0)
                gr.append(indentation);

            gr.appendNewLine(line);
        }

        if (_currentLine.length() > 0) {
            gr.append(indentation);
            gr.appendNewLine(_currentLine.toString());
        }
    }

    public void setName(String name){
        _name = name;
    }

    public String getName(){
        return _name;
    }

    public void setPath(String path){
        _path = path;
    }

    public String getPath(){
        return _path;
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < _lines.size(); i++) {
            String line = _lines.get(i);
            sb.append(line);
            sb.append(TemplateHelper.NEWLINE);
        }

        if (_currentLine.length() > 0) {
            sb.append(_currentLine.toString());
            sb.append(TemplateHelper.NEWLINE);
        }

        return sb.toString();
    }
}

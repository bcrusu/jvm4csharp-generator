package com.jvm4csharp.generator;

import java.util.LinkedList;

public class GenerationResult {
    private final LinkedList<String> _lines;
    private final GenerationResultLocation _location;
    private StringBuilder _currentLine;

    public GenerationResult(GenerationResultLocation location) {
        _lines = new LinkedList<>();
        _currentLine = new StringBuilder();
        _location = location;
    }

    public GenerationResult() {
        this(null);
    }

    public void newLine() {
        _lines.add(_currentLine.toString());
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

    public void ensureEmptyLine(boolean condition) {
        if (condition) {
            if (_currentLine.length() > 0)
                newLine();
            newLine();
        }
    }

    public GenerationResultLocation getLocation() {
        return _location;
    }

    public void renderTo(GenerationResult gr, int indentationLevel) {
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

    @Override
    public String toString() {
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

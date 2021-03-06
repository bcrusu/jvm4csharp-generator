package com.jvm4csharp.generator;

import java.util.LinkedList;

public class GenerationResult {
    protected final LinkedList<String> _lines;
    protected final GenerationResultLocation _location;
    protected StringBuilder _currentLine;

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

    public GenerationResultLocation getLocation() {
        return _location;
    }

    public void renderTo(GenerationResult gr, int indentationLevel) {
        String indentation = TemplateHelper.getIndentation(indentationLevel);
        for (String line : _lines) {
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
        for (String line : _lines) {
            sb.append(line);
            sb.append(TemplateHelper.NEWLINE);
        }

        if (_currentLine.length() > 0) {
            sb.append(_currentLine.toString());
            sb.append(TemplateHelper.NEWLINE);
        }

        return sb.toString();
    }

    public boolean isEmpty() {
        if (_currentLine.length() > 0)
            return false;

        if (_lines.size() > 0)
            return false;

        return true;
    }
}

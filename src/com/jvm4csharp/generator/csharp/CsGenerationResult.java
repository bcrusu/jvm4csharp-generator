package com.jvm4csharp.generator.csharp;

import com.jvm4csharp.generator.GenerationResult;
import com.jvm4csharp.generator.GenerationResultLocation;
import com.jvm4csharp.generator.TemplateHelper;

import java.util.HashSet;
import java.util.Set;

public class CsGenerationResult extends GenerationResult {
    private final Set<String> _usedNamespaces;

    public CsGenerationResult(GenerationResultLocation location) {
        super(location);
        _usedNamespaces = new HashSet<>();
    }

    public CsGenerationResult() {
        this(null);
    }

    public void ensureEmptyLine() {
        if (_currentLine.length() > 0)
            newLine();

        if (_lines.size() > 0) {
            String lastLine = _lines.getLast();
            if (lastLine.endsWith(TemplateHelper.BLOCK_CLOSE) || lastLine.endsWith(";"))
                newLine();
        }
    }

    public void cleanEndLines() {
        if (_currentLine.toString().trim().length() > 0) {
            newLine();
            return;
        }

        while (_lines.getLast().isEmpty())
            _lines.removeLast();
    }

    public void addUsedNamespace(String namespace) {
        _usedNamespaces.add(namespace);
    }

    public Set<String> getUsedNamespaces() {
        return _usedNamespaces;
    }

    @Override
    public void renderTo(GenerationResult gr, int indentationLevel) {
        super.renderTo(gr, indentationLevel);

        if (gr instanceof CsGenerationResult)
            ((CsGenerationResult) gr)._usedNamespaces.addAll(_usedNamespaces);
    }
}

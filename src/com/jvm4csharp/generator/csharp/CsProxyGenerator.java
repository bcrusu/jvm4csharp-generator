package com.jvm4csharp.generator.csharp;

import com.jvm4csharp.generator.GenerationResult;
import com.jvm4csharp.generator.GenerationResultLocation;
import com.jvm4csharp.generator.IProxyGenerator;
import com.jvm4csharp.generator.TemplateHelper;
import com.jvm4csharp.generator.reflectx.XClassDefinition;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class CsProxyGenerator implements IProxyGenerator {
    private final String _namespacePrefix;

    public CsProxyGenerator(String namespacePrefix) {
        _namespacePrefix = namespacePrefix;
    }

    @Override
    public GenerationResult generate(XClassDefinition classDefinition) {
        GenerationResultLocation location = getLocation(classDefinition);

        CsGenerationResult result = new CsGenerationResult(location);

        generateClassDefinition(result, classDefinition);

        return result;
    }

    private String[] getNamespacesUsed(CsGenerationResult result, String currentNamespace) {
        Set<String> usedNamespaces = result.getUsedNamespaces();

        Set<String> set = new HashSet<>();
        set.add("jvm4csharp");

        for (String namespace : usedNamespaces) {
            if (_namespacePrefix != null && !namespace.startsWith(_namespacePrefix))
                namespace = _namespacePrefix + "." + namespace;

            set.add(namespace);
        }

        return set.stream()
                .filter(x -> x.compareTo(currentNamespace) != 0)
                .filter(x -> !currentNamespace.startsWith(x))
                .sorted()
                .toArray(String[]::new);
    }

    private static GenerationResultLocation getLocation(XClassDefinition classDefinition) {
        String packageName = classDefinition.getXClass().getPackageName();

        String name = classDefinition.getXClass().getSimpleName() + ".gen.cs";
        String path = packageName.replace(".", File.separator);

        return new GenerationResultLocation(path, name);
    }

    private void generateClassDefinition(CsGenerationResult result, XClassDefinition classDefinition) {
        String currentNamespace = CsType.getNamespace(classDefinition.getXClass());
        if (_namespacePrefix != null)
            currentNamespace = _namespacePrefix + "." + currentNamespace;

        CsGenerationResult classRenderResult = new CsGenerationResult();
        CsTemplateHelper.renderClassDefinition(classRenderResult, classDefinition);

        // using statements
        String[] namespacesUsed = getNamespacesUsed(classRenderResult, currentNamespace);
        if (namespacesUsed.length > 0) {
            for (String namespaceUsed : namespacesUsed) {
                result.append("using ");
                result.append(namespaceUsed);
                result.appendNewLine(";");
            }

            result.newLine();
        }

        result.appendNewLine("// ReSharper disable InconsistentNaming");

        // namespace block
        result.append("namespace ");
        result.appendNewLine(currentNamespace);
        result.appendNewLine(TemplateHelper.BLOCK_OPEN);

        classRenderResult.renderTo(result, 1);
        result.cleanEndLines();

        result.appendNewLine(TemplateHelper.BLOCK_CLOSE);
    }
}

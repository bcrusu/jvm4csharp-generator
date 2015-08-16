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
        ICsTemplate template = CsTemplateFactory.createTemplate(classDefinition);
        GenerationResultLocation location = getLocation(classDefinition);

        GenerationResult result = new GenerationResult(location);

        generateClassDefinition(result, classDefinition, template);

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

        String name = CsType.getSimpleTypeName(classDefinition.getXClass()) + ".gen.cs";
        String path = packageName.replace(".", File.separator);

        return new GenerationResultLocation(path, name);
    }

    private void generateClassDefinition(GenerationResult result, XClassDefinition classDefinition, ICsTemplate template) {
        String currentNamespace = CsType.getNamespace(classDefinition.getXClass());
        if (_namespacePrefix != null)
            currentNamespace = _namespacePrefix + "." + currentNamespace;

        CsGenerationResult tmpResult = new CsGenerationResult();

        tmpResult.appendNewLine("// ReSharper disable InconsistentNaming");

        // namespace block
        tmpResult.append("namespace ");
        tmpResult.appendNewLine(currentNamespace);
        tmpResult.appendNewLine(TemplateHelper.BLOCK_OPEN);

        // render C# type
        template.generate().renderTo(tmpResult, 1);

        tmpResult.appendNewLine(TemplateHelper.BLOCK_CLOSE);

        // using statements
        String[] namespacesUsed = getNamespacesUsed(tmpResult, currentNamespace);
        if (namespacesUsed.length > 0) {
            for (String namespaceUsed : namespacesUsed) {
                result.append("using ");
                result.append(namespaceUsed);
                result.appendNewLine(";");
            }

            result.newLine();
        }

        tmpResult.renderTo(result, 0);
    }
}

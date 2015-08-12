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

        //TODO: generate companion templates

        return generateTemplate(classDefinition, template, location);
    }

    private String[] getNamespacesUsed(XClassDefinition clazz, String currentNamespace) {
        Set<String> referencedPackageNames = clazz.getReferencedPackageNames();

        Set<String> set = new HashSet<>();
        set.add("jvm4csharp");
        set.add("jvm4csharp.ArrayUtils");

        for (String referencedPackageName : referencedPackageNames) {
            String namespace = CsType.renderNamespace(referencedPackageName);

            if (_namespacePrefix != null && !namespace.startsWith(_namespacePrefix))
                namespace = _namespacePrefix + "." + namespace;

            set.add(namespace);
        }

        return set.stream()
                .filter(x -> x.compareTo(currentNamespace) != 0)
                .filter(x -> !currentNamespace.startsWith(x))
                .sorted()
                .toArray(i -> new String[i]);
    }

    private static GenerationResultLocation getLocation(XClassDefinition classDefinition) {
        String packageName = classDefinition.getXClass().getPackageName();

        String name = CsType.renderSimpleTypeName(classDefinition) + ".gen.cs";
        String path = packageName.replace(".", File.separator);

        GenerationResultLocation result = new GenerationResultLocation(path, name);
        return result;
    }

    private GenerationResult generateTemplate(XClassDefinition classDefinition, ICsTemplate template, GenerationResultLocation location) {
        String currentNamespace = CsType.renderNamespace(classDefinition.getXClass());
        if (_namespacePrefix != null)
            currentNamespace = _namespacePrefix + "." + currentNamespace;

        String[] namespacesUsed = getNamespacesUsed(classDefinition, currentNamespace);

        GenerationResult result = new GenerationResult(location);

        // using statements
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

        // render C# type
        template.generate().renderTo(result, 1);

        result.appendNewLine(TemplateHelper.BLOCK_CLOSE);

        return result;
    }
}

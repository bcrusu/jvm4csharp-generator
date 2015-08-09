package com.jvm4csharp.generator.csharp;

import com.jvm4csharp.generator.*;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class CsProxyGenerator implements IProxyGenerator {
    private final String _namespacePrefix;

    public CsProxyGenerator(String namespacePrefix) {
        _namespacePrefix = namespacePrefix;
    }

    @Override
    public GenerationResult generate(ClassDetails classDetails) {
        ICsTemplate template = CsTemplateFactory.createTemplate(classDetails);
        GenerationResultLocation location = getLocation(classDetails.Class);

        //TODO: generate companion templates

        return generateTemplate(classDetails.Class, template, location);
    }

    @Override
    public boolean canGenerate(Class clazz) {
        return CsTemplateFactory.canCreateTemplate(clazz);
    }

    private String[] getNamespacesUsed(CsType[] referencedCsTypes, String currentNamespace) {
        Set<String> set = new HashSet<>();
        set.add("jvm4csharp");

        for (CsType csType : referencedCsTypes) {
            for (String namespaceUsed : csType.namespacesUsed) {
                if (_namespacePrefix != null && !namespaceUsed.startsWith(_namespacePrefix))
                    namespaceUsed = _namespacePrefix + "." + namespaceUsed;

                set.add(namespaceUsed);
            }
        }

        return set.stream()
                .filter(x -> x.compareTo(currentNamespace) != 0)
                .filter(x -> !currentNamespace.startsWith(x))
                .sorted()
                .toArray(x -> new String[x]);
    }

    private static GenerationResultLocation getLocation(Class clazz){
        String packageName = clazz.getPackage().getName();

        String name = CsType.getCsClassName(clazz) + ".gen.cs";
        String path = packageName.replace(".", File.separator);

        GenerationResultLocation result = new GenerationResultLocation(path, name);
        return result;
    }

    private GenerationResult generateTemplate(Class clazz, ICsTemplate template, GenerationResultLocation location) {
        String currentNamespace = CsType.getCsNamespace(clazz);
        if (_namespacePrefix != null)
            currentNamespace = _namespacePrefix + "." + currentNamespace;

        CsType[] csTypes = template.getReferencedCsTypes();
        String[] namespacesUsed = getNamespacesUsed(csTypes, currentNamespace);

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

package com.jvm4csharp.generator.csharp;

import com.jvm4csharp.generator.GenerateResult;
import com.jvm4csharp.generator.IProxyGenerator;
import com.jvm4csharp.generator.TemplateHelper;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

public class CsProxyGenerator implements IProxyGenerator {
    private final String _namespacePrefix;

    public CsProxyGenerator(String namespacePrefix) {
        _namespacePrefix = namespacePrefix;
    }

    @Override
    public GenerateResult generate(Class clazz) {
        ICsTemplate template = CsTemplateFactory.createTemplate(clazz);
        String packageName = clazz.getPackage().getName();
        String currentNamespace = packageName;
        if (_namespacePrefix != null)
            currentNamespace = _namespacePrefix + "." + currentNamespace;

        CsType[] csTypes = template.getReferencedCsTypes();
        String[] namespacesUsed = getNamespacesUsed(csTypes, currentNamespace);

        GenerateResult result = new GenerateResult();
        result.setName(clazz.getSimpleName() + ".generated.cs");
        result.setPath(packageName.replace(".", File.separator));

        // using statements
        if (namespacesUsed.length > 0) {
            for (String namespaceUsed : namespacesUsed) {
                result.append("using ");
                result.append(namespaceUsed);
                result.appendNewLine(";");
            }

            result.newLine();
        }

        // namespace block
        result.append("namespace ");
        result.appendNewLine(currentNamespace);
        result.appendNewLine(TemplateHelper.BLOCK_OPEN);

        // render C# type
        template.generate().renderTo(result, 1);

        result.appendNewLine(TemplateHelper.BLOCK_CLOSE);

        return result;
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
}

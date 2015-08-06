package com.jvm4csharp.generator.csharp;

import com.jvm4csharp.generator.GenerateResult;
import com.jvm4csharp.generator.IProxyGenerator;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

public class CsProxyGenerator implements IProxyGenerator {
    @Override
    public GenerateResult[] generate(Class clazz) {
        ICsTemplate[] templates = CsTemplateFactory.createTemplates(clazz);
        LinkedList<GenerateResult> results = new LinkedList<>();

        for (ICsTemplate template : templates) {
            GenerateResult generateResult = template.generate();
            CsType[] csTypes = template.getReferencedCsTypes();
            String[] namespacesUsed = getNamespacesUsed(csTypes);

            GenerateResult result = new GenerateResult();
            result.setName();
            result.setPath();

            // render
            for (String namespace : namespacesUsed) {
                result.append("using ");
                result.append(namespace);
                result.appendNewLine(";");
            }

            result.newLine();
            generateResult.renderTo(result, 0);
        }

        return results.toArray(new GenerateResult[results.size()]);
    }

    @Override
    public boolean canGenerate(Class clazz) {
        return CsTemplateFactory.canCreateTemplate(clazz);
    }

    private String[] getNamespacesUsed(CsType[] referencedCsTypes) {
        Set<String> set = new HashSet<>();
        for (CsType csType : referencedCsTypes)
            set.addAll(csType.namespacesUsed);

        return set.stream().sorted()
                .toArray(x -> new String[x]);
    }
}

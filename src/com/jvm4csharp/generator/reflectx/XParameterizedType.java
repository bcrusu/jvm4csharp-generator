package com.jvm4csharp.generator.reflectx;

import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.ParameterizedType;
import java.text.CollationElementIterator;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class XParameterizedType extends XType {
    private final ParameterizedType _parameterizedType;
    private final XClass _rawType;
    private final List<XEditableType> _actualTypeArguments;

    XParameterizedType(ParameterizedType parameterizedType) {
        _parameterizedType = parameterizedType;
        _rawType = (XClass) XTypeFactory.createType(parameterizedType.getRawType());
        _actualTypeArguments = Arrays.asList(parameterizedType.getActualTypeArguments())
                .stream()
                .map(XTypeFactory::createEditableType)
                .collect(Collectors.toList());
    }

    private XParameterizedType(XParameterizedType toClone) {
        _parameterizedType = toClone._parameterizedType;
        _rawType = toClone._rawType;
        _actualTypeArguments = toClone._actualTypeArguments
                .stream()
                .map(x -> x.clone())
                .collect(Collectors.toList());
    }

    @Override
    void replaceTypeVariable(GenericDeclaration variableOwner, String variableName, XType newType) {
        for (XEditableType _actualTypeArgument : _actualTypeArguments)
            _actualTypeArgument.replaceTypeVariable(variableOwner, variableName, newType);
    }

    @Override
    void renameTypeVariable(GenericDeclaration variableOwner, String oldName, String newName) {
        for (XEditableType _actualTypeArgument : _actualTypeArguments)
            _actualTypeArgument.renameTypeVariable(variableOwner, oldName, newName);
    }

    @Override
    public Set<String> getReferencedPackageNames() {
        HashSet<String> result = new HashSet<>();
        result.addAll(_rawType.getReferencedPackageNames());

        for (XType xType : getActualTypeArguments())
            result.addAll(xType.getReferencedPackageNames());

        return result;
    }

    @Override
    public XTypeCompareResult compareTo(XType other) {
        if (!(other instanceof XParameterizedType))
            return XTypeCompareResult.NotEqual;

        XParameterizedType other2 = (XParameterizedType) other;
        if (_rawType.compareTo(other2._rawType) != XTypeCompareResult.Equal)
            return XTypeCompareResult.NotEqual;

        List<XType> thisActualTypeArguments = getActualTypeArguments();
        List<XType> otherActualTypeArguments = other2.getActualTypeArguments();

        if (thisActualTypeArguments.size() != otherActualTypeArguments.size())
            return XTypeCompareResult.NotEqual;

        for (int i = 0; i < thisActualTypeArguments.size(); i++)
            if (thisActualTypeArguments.get(i).compareTo(otherActualTypeArguments.get(i)) != XTypeCompareResult.Equal)
                return XTypeCompareResult.NotEqual;

        return XTypeCompareResult.Equal;
    }

    @Override
    public XType clone() {
        return new XParameterizedType(this);
    }

    @Override
    protected String getDisplayName() {
        StringBuilder sb = new StringBuilder();
        sb.append(_parameterizedType.getRawType().getTypeName());
        sb.append("<");

        for (int i = 0; i < _actualTypeArguments.size(); i++) {
            sb.append(_actualTypeArguments.get(i).getType().getDisplayName());

            if (i < _actualTypeArguments.size() - 1)
                sb.append(", ");
        }

        sb.append(">");
        return sb.toString();
    }

    public XClass getRawType() {
        return _rawType;
    }

    public List<XType> getActualTypeArguments() {
        return _actualTypeArguments.stream()
                .map(XEditableType::getType)
                .collect(Collectors.toList());
    }
}

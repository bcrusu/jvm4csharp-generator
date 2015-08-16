package com.jvm4csharp.generator.reflectx;

import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.stream.Collectors;

public class XParameterizedType extends XType {
    private final ParameterizedType _parameterizedType;
    private final XClass _rawType;
    private final List<XType> _actualTypeArguments;

    XParameterizedType(XTypeFactory typeFactory, ParameterizedType parameterizedType) {
        _parameterizedType = parameterizedType;
        _rawType = (XClass) typeFactory.getType(parameterizedType.getRawType());
        _actualTypeArguments = Arrays.asList(parameterizedType.getActualTypeArguments())
                .stream()
                .map(typeFactory::getType)
                .collect(Collectors.toList());
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof XParameterizedType))
            return false;

        XParameterizedType other2 = (XParameterizedType) other;
        if (!_rawType.equals(other2._rawType))
            return false;

        List<XType> thisActualTypeArguments = getActualTypeArguments();
        List<XType> otherActualTypeArguments = other2.getActualTypeArguments();

        if (thisActualTypeArguments.size() != otherActualTypeArguments.size())
            return false;

        for (int i = 0; i < thisActualTypeArguments.size(); i++)
            if (!thisActualTypeArguments.get(i).equals(otherActualTypeArguments.get(i)))
                return false;

        return true;
    }

    @Override
    protected String getDisplayName() {
        StringBuilder sb = new StringBuilder();
        sb.append(_parameterizedType.getRawType().getTypeName());
        sb.append("<");

        for (int i = 0; i < _actualTypeArguments.size(); i++) {
            sb.append(_actualTypeArguments.get(i).getDisplayName());

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
        return _actualTypeArguments;
    }
}

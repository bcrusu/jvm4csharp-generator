package com.jvm4csharp.generator.reflectx;

import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.Objects;

public class XWildcardType extends XType {
    private final XTypeFactory _typeFactory;
    private final WildcardType _wildcardType;
    private XType _upperBound;
    private XType _lowerBound;

    XWildcardType(XTypeFactory typeFactory, WildcardType wildcardType) {
        _typeFactory = typeFactory;
        _wildcardType = wildcardType;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof XWildcardType))
            return false;

        XWildcardType other2 = (XWildcardType) other;
        if (!Objects.equals(getLowerBound(), other2.getLowerBound()))
            return false;

        if (!Objects.equals(getUpperBound(), other2.getUpperBound()))
            return false;

        return true;
    }

    @Override
    protected String getDisplayName() {
        StringBuilder sb = new StringBuilder();
        sb.append("?");

        XClass objectClass = XClassFactory.getClass(Object.class);

        XType upperBound = getUpperBound();
        if (upperBound != null && !upperBound.equals(objectClass)) {
            sb.append(" extends ");
            sb.append(upperBound.getDisplayName());
        }

        XType lowerBound = getLowerBound();
        if (lowerBound != null && !lowerBound.equals(objectClass)) {
            sb.append(" super ");
            sb.append(lowerBound.getDisplayName());
        }

        return sb.toString();
    }

    public XType getUpperBound() {
        if (_upperBound == null) {
            Type[] upperBounds = _wildcardType.getUpperBounds();
            if (upperBounds.length == 0)
                return null;

            if (upperBounds.length > 1)
                throw new IllegalArgumentException("Invalid WildcardType detected. Too many upper bounds.");

            _upperBound = _typeFactory.getType(upperBounds[0]);
        }

        return _upperBound;
    }

    public XType getLowerBound() {
        if (_lowerBound == null) {
            Type[] lowerBounds = _wildcardType.getLowerBounds();
            if (lowerBounds.length == 0)
                return null;

            if (lowerBounds.length > 1)
                throw new IllegalArgumentException("Invalid WildcardType detected. Too many lower bounds.");

            _lowerBound = _typeFactory.getType(lowerBounds[0]);
        }

        return _lowerBound;
    }

    public boolean hasBounds() {
        return getUpperBound() != null || getLowerBound() != null;
    }
}

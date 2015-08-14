package com.jvm4csharp.generator.reflectx;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public interface IClassMemberFilter {
    boolean isIncluded(XField field);

    boolean isIncluded(XConstructor constructor);

    boolean isIncluded(XMethod method);

    class NullClassMemberFilter implements IClassMemberFilter {
        @Override
        public boolean isIncluded(XField field) {
            return true;
        }

        @Override
        public boolean isIncluded(XConstructor constructor) {
            return true;
        }

        @Override
        public boolean isIncluded(XMethod method) {
            return true;
        }
    }
}

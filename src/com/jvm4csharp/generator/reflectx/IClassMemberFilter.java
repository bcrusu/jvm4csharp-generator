package com.jvm4csharp.generator.reflectx;

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

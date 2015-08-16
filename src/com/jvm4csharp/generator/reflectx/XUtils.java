package com.jvm4csharp.generator.reflectx;

import java.lang.reflect.Executable;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

class XUtils {
    static final int SYNTHETIC = 4096;
    static final int BRIDGE = 64;

    static boolean isPublic(int modifier) {
        return Modifier.isPublic(modifier);
    }

    static boolean isFinal(int modifier) {
        return Modifier.isFinal(modifier);
    }

    static boolean isStatic(int modifier) {
        return Modifier.isStatic(modifier);
    }

    static boolean isAbstract(int modifier) {
        return Modifier.isAbstract(modifier);
    }

    static boolean isPublic(Member member) {
        return isPublic(member.getModifiers());
    }

    static boolean isPublic(Class clazz) {
        return isPublic(clazz.getModifiers());
    }

    static boolean isFinal(Member member) {
        return isFinal(member.getModifiers());
    }

    static boolean isFinal(Class clazz) {
        return isFinal(clazz.getModifiers());
    }

    static boolean isStatic(Member member) {
        return isStatic(member.getModifiers());
    }

    static boolean isStatic(Class clazz) {
        return isStatic(clazz.getModifiers());
    }

    static boolean isAbstract(Member member) {
        return isAbstract(member.getModifiers());
    }

    static boolean isAbstract(Class clazz) {
        return Modifier.isAbstract(clazz.getModifiers());
    }

    static boolean isCompilerGenerated(Member member) {
        int modifiers = member.getModifiers();
        return ((modifiers & SYNTHETIC) != 0) || ((modifiers & BRIDGE) != 0);
    }

    static String getInternalTypeName(Class clazz) {
        if (clazz == Void.TYPE) {
            return "V";
        }

        String result = "";
        if (clazz.isArray()) {
            result = "[";
            clazz = clazz.getComponentType();
        }

        if (clazz.isPrimitive()) {
            if (clazz == Boolean.TYPE)
                result += "Z";
            else if (clazz == Byte.TYPE)
                result += "B";
            else if (clazz == Character.TYPE)
                result += "C";
            else if (clazz == Short.TYPE)
                result += "S";
            else if (clazz == Integer.TYPE)
                result += "I";
            else if (clazz == Long.TYPE)
                result += "J";
            else if (clazz == Float.TYPE)
                result += "F";
            else if (clazz == Double.TYPE)
                result += "D";
            else
                throw new Error("Unrecognized primitive type.");
        } else {
            String className = clazz.getCanonicalName();
            className = className.replace('.', '/');
            result += 'L' + className + ';';
        }

        return result;
    }

    static String getInternalSignature(Executable executable) {
        StringBuilder result = new StringBuilder();
        result.append('(');

        Class[] parameterTypes = executable.getParameterTypes();
        for (Class parameterType : parameterTypes)
            result.append(getInternalTypeName(parameterType));

        result.append(')');
        return result.toString();
    }

    public static Set<String> getReferencedPackageNames(XType type) {
        class tmp {
            private final List<XType> _seenTypes;
            private final Set<String> _packageNames;

            public tmp() {
                _seenTypes = new LinkedList<>();
                _packageNames = new HashSet<>();
            }

            public Set<String> getReferencedPackageNames(XType forType) {
                work(forType);
                return _packageNames;
            }

            private void work(XType forType) {
                if (forType instanceof XTypeVariable)
                    forType = ((XTypeVariable) forType).getResolvedType();

                if (_seenTypes.contains(forType))
                    return;
                _seenTypes.add(forType);

                if (forType instanceof XClass) {
                    XClass clazz = (XClass) forType;

                    if (clazz.isVoid() || clazz.isPrimitive())
                        return;

                    if (clazz.isArray()) {
                        work(clazz.getArrayComponentType());
                    } else {
                        _packageNames.add(clazz.getPackageName());
                    }
                } else if (forType instanceof XParameterizedType) {
                    XParameterizedType parameterizedType = (XParameterizedType) forType;
                    work(parameterizedType.getRawType());

                    for (XType item : parameterizedType.getActualTypeArguments())
                        work(item);
                } else if (forType instanceof XTypeVariable) {
                    XType resolvedType = ((XTypeVariable) forType).getResolvedType();

                    if (!(resolvedType instanceof XTypeVariable)) {
                        work(resolvedType);
                        return;
                    }

                    XTypeVariable typeVariable = (XTypeVariable) resolvedType;

                    for (XType bound : typeVariable.getBounds())
                        work(bound);
                } else if (forType instanceof XWildcardType) {
                } else if (forType instanceof XGenericArrayType) {
                    XGenericArrayType genericArrayType = (XGenericArrayType) forType;
                    work(genericArrayType.getGenericComponentType());
                }
            }
        }

        return new tmp().getReferencedPackageNames(type);
    }

    public static boolean areTypesEquivalent(XType type1, XType type2) {
        class XTypeTuple {
            private final XType _item1;
            private final XType _item2;

            XTypeTuple(XType item1, XType item2) {
                _item1 = item1;
                _item2 = item2;
            }

            @Override
            public boolean equals(Object other) {
                if (!(other instanceof XTypeTuple))
                    return false;
                XTypeTuple other2 = (XTypeTuple) other;
                return _item1.equals(other2._item1) && _item2.equals(other2._item2);
            }
        }

        class tmp {
            private final List<XTypeTuple> _seenPairs;

            public tmp() {
                _seenPairs = new LinkedList<>();
            }

            public boolean areTypesEquivalent(XType type1, XType type2) {
                if (type1 instanceof XTypeVariable)
                    type1 = ((XTypeVariable) type1).getResolvedType();
                if (type2 instanceof XTypeVariable)
                    type2 = ((XTypeVariable) type2).getResolvedType();

                XTypeTuple tuple = new XTypeTuple(type1, type2);
                if (_seenPairs.contains(tuple))
                    return true;
                _seenPairs.add(tuple);


                if (type1 instanceof XClass) {
                    XClass clazz = (XClass) type1;
                    return clazz.equals(type2);
                } else if (type1 instanceof XParameterizedType) {
                    if (!(type2 instanceof XParameterizedType))
                        return false;

                    XParameterizedType pType1 = (XParameterizedType) type1;
                    XParameterizedType pType2 = (XParameterizedType) type2;

                    if (!areTypesEquivalent(pType1.getRawType(), pType2.getRawType()))
                        return false;

                    List<XType> pType1ActualTypeArguments = pType1.getActualTypeArguments();
                    List<XType> pType2ActualTypeArguments = pType2.getActualTypeArguments();

                    if (pType1ActualTypeArguments.size() != pType2ActualTypeArguments.size())
                        return false;

                    for (int i = 0; i < pType1ActualTypeArguments.size(); i++)
                        if (!areTypesEquivalent(pType1ActualTypeArguments.get(i), pType2ActualTypeArguments.get(i)))
                            return false;

                    return true;
                } else if (type1 instanceof XTypeVariable) {
                    if (!(type2 instanceof XTypeVariable))
                        return false;

                    XType resolvedType1 = ((XTypeVariable) type1).getResolvedType();
                    XType resolvedType2 = ((XTypeVariable) type2).getResolvedType();

                    if (resolvedType1 instanceof XTypeVariable && resolvedType2 instanceof XTypeVariable) {
                        XTypeVariable resolvedVariable1 = (XTypeVariable) resolvedType1;
                        XTypeVariable resolvedVariable2 = (XTypeVariable) resolvedType2;

                        List<XType> bounds1 = resolvedVariable1.getBounds();
                        List<XType> bounds2 = resolvedVariable2.getBounds();

                        if (bounds1.size() != bounds2.size())
                            return false;

                        for (int i = 0; i < bounds1.size(); i++)
                            if (!areTypesEquivalent(bounds1.get(i), bounds2.get(i)))
                                return false;

                        return true;
                    } else
                        return areTypesEquivalent(resolvedType1, resolvedType2);
                } else if (type1 instanceof XWildcardType) {
                    if (!(type2 instanceof XWildcardType))
                        return false;

                    XWildcardType wildcardType1 = (XWildcardType) type1;
                    XWildcardType wildcardType2 = (XWildcardType) type2;

                    XType lowerBound1 = wildcardType1.getLowerBound();
                    XType lowerBound2 = wildcardType2.getLowerBound();

                    if (lowerBound1 == null) {
                        if (lowerBound2 != null)
                            return false;
                    } else if (!areTypesEquivalent(lowerBound1, lowerBound2))
                        return false;

                    XType upperBound1 = wildcardType1.getUpperBound();
                    XType upperBound2 = wildcardType2.getUpperBound();

                    if (upperBound1 == null) {
                        if (upperBound2 != null)
                            return false;
                    } else if (!areTypesEquivalent(upperBound1, upperBound2))
                        return false;

                    return true;
                } else if (type1 instanceof XGenericArrayType) {
                    if (!(type2 instanceof XGenericArrayType))
                        return false;

                    XGenericArrayType genericArrayType1 = (XGenericArrayType) type1;
                    XGenericArrayType genericArrayType2 = (XGenericArrayType) type2;

                    return areTypesEquivalent(genericArrayType1.getGenericComponentType(), genericArrayType2.getGenericComponentType());
                } else
                    throw new IllegalArgumentException();
            }
        }

        return new tmp().areTypesEquivalent(type1, type2);
    }

    static boolean getMethodsAreEquivalent(Method method1, Method method2) {
        if ((!Objects.equals(method1.getName(), method2.getName())))
            return false;

        if (!method1.getReturnType().equals(method2.getReturnType()))
            return false;

        return equalParamTypes(method1.getParameterTypes(), method2.getParameterTypes());
    }

    private static boolean equalParamTypes(Class[] params1, Class[] params2) {
        if (params1.length == params2.length) {
            for (int i = 0; i < params1.length; i++) {
                if (params1[i] != params2[i])
                    return false;
            }
            return true;
        }
        return false;
    }
}

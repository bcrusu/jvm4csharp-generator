package com.jvm4csharp.generator.reflectx;

import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;

public class XClassDefinition implements IGenericDeclaration {
    private final XTypeFactory _typeFactory;
    private final Class _class;
    private final XClass _xClass;
    private final List<XType> _actualTypeArguments;
    private final List<XField> _fields;
    private final List<XMethod> _declaredMethods;
    private final List<XConstructor> _constructors;
    private final List<XTypeVariable> _typeParameters;
    private XClassDefinition _superclass;
    private List<XClassDefinition> _implementedInterfaces;

    private XClassDefinition(XTypeFactory typeFactory, Class clazz, Type[] actualTypeArguments) {
        _typeFactory = typeFactory;
        _class = clazz;
        _xClass = XClassFactory.getClass(clazz);
        _typeParameters = Arrays.asList(_class.getTypeParameters())
                .stream()
                .map(_typeFactory::getType)
                .map(x -> (XTypeVariable) x)
                .collect(Collectors.toList());
        _actualTypeArguments = Arrays.asList(actualTypeArguments)
                .stream()
                .map(typeFactory::getType)
                .collect(Collectors.toList());
        _fields = getPublicFields(this);
        _constructors = getPublicConstructors(this);
        _declaredMethods = getPublicDeclaredMethods(this);
        _implementedInterfaces = getPublicImplementedInterfaces(clazz);

        typeFactory.renameCollidingTypeVariablesForMembers(clazz);
    }

    XClassDefinition(XTypeFactory typeFactory, Class clazz) {
        this(typeFactory, clazz, new Type[0]);
    }

    XClassDefinition(XTypeFactory typeFactory, ParameterizedType parameterizedType) {
        this(typeFactory, (Class) parameterizedType.getRawType(), parameterizedType.getActualTypeArguments());
    }

    XTypeFactory getTypeFactory() {
        return _typeFactory;
    }

    public Set<String> getReferencedPackageNames() {
        HashSet<String> result = new HashSet<>();

        result.addAll(getReferencedPackageNamesInDefinition());

        for (XField field : getFields())
            result.addAll(field.getReferencedPackageNames());

        if (!_xClass.isInterface())
            for (XConstructor constructor : getConstructors())
                result.addAll(constructor.getReferencedPackageNames());

        for (XMethod method : getDeclaredMethods())
            result.addAll(method.getReferencedPackageNames());

        return result;
    }

    public Set<String> getReferencedPackageNamesInDefinition() {
        HashSet<String> result = new HashSet<>();

        if (!_xClass.isInterface()) {
            XClassDefinition superclass = getSuperclass();
            result.addAll(superclass.getXClass().getReferencedPackageNames());

            for (XType typeArgument : getSuperclass().getActualTypeArguments())
                result.addAll(typeArgument.getReferencedPackageNames());
        }

        for (XClassDefinition implementedInterface : getImplementedInterfaces()) {
            result.addAll(implementedInterface.getXClass().getReferencedPackageNames());

            for (XType typeArgument : implementedInterface.getActualTypeArguments())
                result.addAll(typeArgument.getReferencedPackageNames());
        }

        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof XClassDefinition))
            return false;

        XClassDefinition other2 = (XClassDefinition) other;
        if (_class != other2._class)
            return false;

        List<XType> thisActualTypeArguments = _actualTypeArguments;
        List<XType> otherActualTypeArguments = other2._actualTypeArguments;

        if (thisActualTypeArguments.size() != otherActualTypeArguments.size())
            return false;

        for (int i = 0; i < thisActualTypeArguments.size(); i++)
            if (thisActualTypeArguments.get(i).compareTo(otherActualTypeArguments.get(i)) != XTypeCompareResult.Equal)
                return false;

        return true;
    }

    @Override
    public String toString() {
        return getXClass().toString();
    }

    public XClass getXClass() {
        return _xClass;
    }

    @Override
    public List<XTypeVariable> getTypeParameters() {
        return _typeParameters;
    }

    @Override
    public boolean hasTypeParameters() {
        return getTypeParameters().size() > 0;
    }

    public List<XType> getActualTypeArguments() {
        return _actualTypeArguments;
    }

    public List<XField> getFields() {
        return _fields;
    }

    public List<XMethod> getDeclaredMethods() {
        return _declaredMethods;
    }

    public List<XConstructor> getConstructors() {
        return _constructors;
    }

    public XClassDefinition getSuperclass() {
        if (Object.class == _class)
            return null;

        if (_superclass == null)
            _superclass = getPublicSuperclass(_class);
        return _superclass;
    }

    public List<XClassDefinition> getImplementedInterfaces() {
        return _implementedInterfaces;
    }

    private static XClassDefinition getPublicSuperclass(Class clazz) {
        Class superclass = clazz.getSuperclass();
        Type genericSuperclass = clazz.getGenericSuperclass();

        // find the 1st public superclass
        while (!XUtils.isPublic(superclass)) {
            genericSuperclass = superclass.getGenericSuperclass();
            superclass = superclass.getSuperclass();
        }

        return XClassDefinitionFactory.createClassDefinition(genericSuperclass);
    }

    private List<XClassDefinition> getPublicImplementedInterfaces(Class clazz) {
        List<XClassDefinition> publicInterfaces = new LinkedList<>();
        if (Object.class == clazz)
            return publicInterfaces;

        // API docs states for both methods: "The order of the interface objects in the array corresponds to the
        // order of the interface names in the implements clause of the declaration of the class"
        Class[] interfaces = clazz.getInterfaces();
        Type[] genericInterfaces = clazz.getGenericInterfaces();
        for (int i = 0; i < interfaces.length; i++) {
            Class currentInterface = interfaces[i];

            if (XUtils.isPublic(currentInterface))
                publicInterfaces.add(XClassDefinitionFactory.createClassDefinition(_typeFactory, genericInterfaces[i]));
            else {
                // expand public interfaces from private implemented interface
                Collection<XClassDefinition> publicInterfacesFromPrivateInterface = getPublicImplementedInterfaces(currentInterface);

                if (genericInterfaces[i] instanceof ParameterizedType)
                    _typeFactory.replaceActualTypeArguments((ParameterizedType) genericInterfaces[i]);

                publicInterfaces.addAll(publicInterfacesFromPrivateInterface);
            }
        }

        // expand public interfaces from private superclass
        if (!clazz.isInterface()) {
            Class superclass = clazz.getSuperclass();
            Type genericSuperclass = clazz.getGenericSuperclass();

            if (!XUtils.isPublic(superclass)) {
                Collection<XClassDefinition> publicInterfacesFromPrivateSuperclass = getPublicImplementedInterfaces(superclass);

                if (genericSuperclass instanceof ParameterizedType)
                    _typeFactory.replaceActualTypeArguments((ParameterizedType) genericSuperclass);

                publicInterfaces.addAll(publicInterfacesFromPrivateSuperclass);
            }
        }

        List<XClassDefinition> result = new LinkedList<>();

        // remove duplicates
        for (XClassDefinition iface : publicInterfaces) {
            if (!result.contains(iface))
                result.add(iface);
        }

        return result.stream()
                .collect(Collectors.toList());
    }

    private static List<XField> getPublicFields(XClassDefinition classDefinition) {
        return Arrays.asList(classDefinition._class.getDeclaredFields())
                .stream()
                .filter(XUtils::isPublic)
                .map(x -> new XField(classDefinition, x))
                .collect(Collectors.toList());
    }

    private static List<XConstructor> getPublicConstructors(XClassDefinition classDefinition) {
        return Arrays.asList(classDefinition._class.getDeclaredConstructors())
                .stream()
                .filter(XUtils::isPublic)
                .map(x -> new XConstructor(classDefinition, x))
                .collect(Collectors.toList());
    }

    private static List<XMethod> getPublicDeclaredMethods(XClassDefinition classDefinition) {
        List<Method> result = new LinkedList<>();
        Method[] declaredMethods = classDefinition._class.getDeclaredMethods();

        Class superclass = classDefinition._class.getSuperclass();
        if (superclass != null) {
            Method[] superclassMethods = superclass.getDeclaredMethods();
            for (Method declaredMethod : declaredMethods) {
                boolean found = false;
                for (Method superclassMethod : superclassMethods)
                    if (XUtils.getMethodsAreEquivalent(declaredMethod, superclassMethod)) {
                        found = true;
                        break;
                    }

                if (!found)
                    result.add(declaredMethod);
            }
        }

        return result
                .stream()
                .filter(XUtils::isPublic)
                .map(x -> new XMethod(classDefinition, x))
                .collect(Collectors.toList());
    }
}

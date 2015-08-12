package com.jvm4csharp.generator.reflectx;

import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;

public class XClassDefinition implements XGenericDeclaration {
    private final Class _class;
    private final XClass _xClass;
    private final List<XEditableType> _actualTypeArguments;
    private XClassDefinition _superclass;
    private List<XField> _fields;
    private List<XMethod> _declaredMethods;
    private List<XConstructor> _constructors;
    private List<XClassDefinition> _implementedInterfaces;

    XClassDefinition(Class clazz) {
        _class = clazz;
        _xClass = (XClass) XTypeFactory.createType(clazz);
        _actualTypeArguments = new ArrayList<>();
    }

    XClassDefinition(ParameterizedType parameterizedType) {
        _class = (Class) parameterizedType.getRawType();
        _xClass = (XClass) XTypeFactory.createType(_class);
        _actualTypeArguments = Arrays.asList(parameterizedType.getActualTypeArguments())
                .stream()
                .map(XTypeFactory::createEditableType)
                .collect(Collectors.toList());
    }

    @Override
    public List<XTypeVariable> getTypeParameters() {
        return getXClass().getTypeParameters();
    }

    private static List<XClassDefinition> getPublicImplementedInterfaces(Class clazz) {
        List<XClassDefinition> publicInterfaces = new LinkedList<>();
        if (Object.class == clazz)
            return publicInterfaces;

        // API docs states for both methods: "The order of the interface objects in the array corresponds to the
        // order of the interface names in the implements clause of the declaration of the class"
        Class[] interfaces = clazz.getInterfaces();
        Type[] genericInterfaces = clazz.getGenericInterfaces();
        for (int i = 0; i < interfaces.length; i++) {
            Class currentInterface = interfaces[i];
            Type currentGenericInterface = genericInterfaces[i];

            if (XUtils.isPublic(currentInterface))
                publicInterfaces.add(XTypeFactory.createClassDefinition(genericInterfaces[i]));
            else {
                // expand public interfaces from private implemented interface
                Collection<XClassDefinition> publicInterfacesFromPrivateInterface = getPublicImplementedInterfaces(currentInterface);
                for (XClassDefinition publicInterface : publicInterfacesFromPrivateInterface)
                    replaceTypeVariables(publicInterface, currentGenericInterface);

                publicInterfaces.addAll(publicInterfacesFromPrivateInterface);
            }
        }

        // expand public interfaces from private superclass
        if (!clazz.isInterface()) {
            Class superclass = clazz.getSuperclass();
            Type genericSuperclass = clazz.getGenericSuperclass();

            if (!XUtils.isPublic(superclass)) {
                Collection<XClassDefinition> publicInterfacesFromPrivateSuperclass = getPublicImplementedInterfaces(superclass);
                for (XClassDefinition publicInterface : publicInterfacesFromPrivateSuperclass)
                    replaceTypeVariables(publicInterface, genericSuperclass);

                publicInterfaces.addAll(publicInterfacesFromPrivateSuperclass);
            }
        }

        List<XClassDefinition> result = new LinkedList<>();

        // remove duplicates
        for (XClassDefinition iface : publicInterfaces) {
            if (!result.contains(iface))
                result.add(iface);
        }

        for (XClassDefinition iface : result)
            iface.renameCollidingTypeVariables(clazz);

        return result.stream()
                .collect(Collectors.toList());
    }

    private static void replaceTypeVariables(XClassDefinition classDefinition, Type genericType) {
        if (genericType instanceof Class)
            return;

        if (genericType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) genericType;
            Class rawType = (Class) parameterizedType.getRawType();

            TypeVariable[] typeParameters = rawType.getTypeParameters();
            Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();

            for (int i = 0; i < actualTypeArguments.length; i++)
                classDefinition.replaceTypeVariable(rawType, typeParameters[i].getName(), XTypeFactory.createType(actualTypeArguments[i]));

            return;
        }

        throw new IllegalArgumentException(String.format("Unhandled type '%1s'.", genericType));
    }

    private static XClassDefinition getPublicSuperclass(Class clazz) {
        Class superclass = clazz.getSuperclass();
        Type genericSuperclass = clazz.getGenericSuperclass();

        // find the 1st public superclass
        while (!XUtils.isPublic(superclass)) {
            genericSuperclass = superclass.getGenericSuperclass();
            superclass = superclass.getSuperclass();
        }

        return XTypeFactory.createClassDefinition(genericSuperclass);
    }

    public Set<String> getReferencedPackageNames() {
        HashSet<String> result = new HashSet<>();

        for (XField field : getFields())
            result.addAll(field.getReferencedPackageNames());

        if (!_xClass.isInterface())
            for (XConstructor constructor : getConstructors())
                result.addAll(constructor.getReferencedPackageNames());

        for (XMethod method : getDeclaredMethods())
            result.addAll(method.getReferencedPackageNames());

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

        List<XEditableType> thisActualTypeArguments = _actualTypeArguments;
        List<XEditableType> otherActualTypeArguments = other2._actualTypeArguments;

        if (thisActualTypeArguments.size() != otherActualTypeArguments.size())
            return false;

        for (int i = 0; i < thisActualTypeArguments.size(); i++)
            if (thisActualTypeArguments.get(i).getType().compareTo(otherActualTypeArguments.get(i).getType()) != XTypeCompareResult.Equal)
                return false;

        return true;
    }

    public XClass getXClass() {
        return _xClass;
    }

    public List<XType> getActualTypeArguments() {
        return _actualTypeArguments.stream()
                .map(XEditableType::getType)
                .collect(Collectors.toList());
    }

    public List<XField> getFields() {
        if (_fields == null)
            _fields = getPublicFields();
        return _fields;
    }

    public List<XMethod> getDeclaredMethods() {
        if (_declaredMethods == null)
            _declaredMethods = getPublicDeclaredMethods();
        return _declaredMethods;
    }

    public List<XConstructor> getConstructors() {
        if (_constructors == null)
            _constructors = getPublicConstructors();
        return _constructors;
    }

    public XClassDefinition getSuperclass() {
        if (_superclass == null)
            _superclass = getPublicSuperclass(_class);
        return _superclass;
    }

    public List<XClassDefinition> getImplementedInterfaces() {
        if (_implementedInterfaces == null)
            _implementedInterfaces = getPublicImplementedInterfaces(_class);

        return _implementedInterfaces
                .stream()
                .collect(Collectors.toList());
    }

    private void replaceTypeVariable(GenericDeclaration genericDeclaration, String variableName, XType newType) {
        for (int i = 0; i < _actualTypeArguments.size(); i++)
            if (_actualTypeArguments.get(i).replaceTypeVariable(genericDeclaration, variableName, newType)) {
                String typeParameterName = _class.getTypeParameters()[i].getName();

                for (XMethod method : getDeclaredMethods())
                    method.replaceTypeVariable(_class, typeParameterName, newType);
            }
    }

    private void renameCollidingTypeVariables(GenericDeclaration genericDeclaration) {
        for (XConstructor constructor : getConstructors())
            constructor.renameCollidingTypeVariables(genericDeclaration);

        for (XMethod method : getDeclaredMethods())
            method.renameCollidingTypeVariables(genericDeclaration);
    }

    private List<XField> getPublicFields() {
        return Arrays.asList(_class.getDeclaredFields())
                .stream()
                .filter(XUtils::isPublic)
                .map(x -> new XField(getXClass(), x))
                .collect(Collectors.toList());
    }

    private List<XConstructor> getPublicConstructors() {
        List<XConstructor> result = Arrays.asList(_class.getDeclaredConstructors())
                .stream()
                .filter(XUtils::isPublic)
                .map(x -> new XConstructor(getXClass(), x))
                .collect(Collectors.toList());

        for (XConstructor constructor : result)
            constructor.renameCollidingTypeVariables(_class);

        return result;
    }

    private List<XMethod> getPublicDeclaredMethods() {
        List<XMethod> result = Arrays.asList(_class.getDeclaredMethods())
                .stream()
                .filter(XUtils::isPublic)
                .filter(x -> x.getDeclaringClass() == _class)
                .map(x -> new XMethod(getXClass(), x))
                .collect(Collectors.toList());

        for (XMethod method : result)
            method.renameCollidingTypeVariables(_class);

        return result;
    }
}

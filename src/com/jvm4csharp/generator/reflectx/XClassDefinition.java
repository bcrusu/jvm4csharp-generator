package com.jvm4csharp.generator.reflectx;

import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;

public class XClassDefinition implements IGenericDeclaration {
    private final Class _class;
    private final XClass _xClass;
    private final List<XEditableType> _actualTypeArguments;
    private final List<XField> _fields;
    private final List<XMethod> _declaredMethods;
    private final List<XConstructor> _constructors;
    private XClassDefinition _superclass;
    private List<XClassDefinition> _implementedInterfaces;

    private XClassDefinition(Class clazz, Type[] actualTypeArguments) {
        _class = clazz;
        _xClass = (XClass) XTypeFactory.createType(clazz);
        _actualTypeArguments = Arrays.asList(actualTypeArguments)
                .stream()
                .map(XTypeFactory::createEditableType)
                .collect(Collectors.toList());

        _fields = getPublicFields(this);
        _constructors = getPublicConstructors(this);
        _declaredMethods = getPublicDeclaredMethods(this);

        handleActualTypeVariablesForMembers();
    }

    XClassDefinition(Class clazz) {
        this(clazz, new Type[0]);
    }

    XClassDefinition(ParameterizedType parameterizedType) {
        this((Class) parameterizedType.getRawType(), parameterizedType.getActualTypeArguments());
    }

    @Override
    public List<XTypeVariable> getTypeParameters() {
        return getXClass().getTypeParameters();
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

    @Override
    public String toString() {
        return getXClass().toString();
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

    public List<XClassDefinition> getImplementedInterfaces() {
        if (_implementedInterfaces == null)
            _implementedInterfaces = getPublicImplementedInterfaces(_class);

        return _implementedInterfaces
                .stream()
                .collect(Collectors.toList());
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

    public void replaceTypeVariable(GenericDeclaration variableOwner, String variableName, XType newType) {
        for (int i = 0; i < _actualTypeArguments.size(); i++)
            if (_actualTypeArguments.get(i).replaceTypeVariable(variableOwner, variableName, newType)) {
                String typeParameterName = _class.getTypeParameters()[i].getName();

                for (XMethod method : getDeclaredMethods())
                    method.replaceTypeVariable(_class, typeParameterName, newType);
            }
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
        return Arrays.asList(classDefinition._class.getDeclaredMethods())
                .stream()
                .filter(XUtils::isPublic)
                .map(x -> new XMethod(classDefinition, x))
                .collect(Collectors.toList());
    }

    private void handleActualTypeVariablesForMembers() {
        Class clazz = _class;
        List<XTypeVariable> typeParameters = getTypeParameters();

        // the order is important:
        // 1st - rename colliding type variables
        for (XConstructor constructor : _constructors)
            constructor.renameCollidingTypeVariables(clazz);

        for (XMethod method : _declaredMethods)
            method.renameCollidingTypeVariables(clazz);

        if (_actualTypeArguments.size() == 0)
            return;

        // 2nd replace actual type variables
        for (int i = 0; i < typeParameters.size(); i++) {
            XType actualTypeArgument = _actualTypeArguments.get(i).getType();
            XTypeVariable typeParameter = typeParameters.get(i);

            for (XField field : _fields)
                field.replaceTypeVariable(clazz, typeParameter.getName(), actualTypeArgument);

            for (XConstructor constructor : _constructors)
                constructor.replaceTypeVariable(clazz, typeParameter.getName(), actualTypeArgument);

            for (XMethod method : _declaredMethods)
                method.replaceTypeVariable(clazz, typeParameter.getName(), actualTypeArgument);
        }
    }
}

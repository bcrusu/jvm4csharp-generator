package com.jvm4csharp.generator;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ClassDetails {
    //TODO: make private
    public final Class Class;
    private List<Field> _fields;
    private List<Method> _methods;
    private List<Constructor> _constructors;
    private List<Type> _implementedInterfaces;

    ClassDetails(Class clazz){
        Class = clazz;
    }

    public List<Field> getFields(){
        if (_fields == null)
            _fields = getDeclaredFieldsInternal(Class);
        return  _fields;
    }

    public List<Method> getMethods(){
        if (_methods == null)
            _methods = getDeclaredMethodsInternal(Class);
        return  _methods;
    }

    public List<Constructor> getConstructors(){
        if (_constructors == null)
            _constructors = getDeclaredConstructorsInternal(Class);
        return  _constructors;
    }

    public List<Type> getImplementedInterfaces(){
        if (_implementedInterfaces == null)
            _implementedInterfaces = getImplementedInterfacesInternal(Class);
        return  _implementedInterfaces;
    }

    public static List<Field> getDeclaredFieldsInternal(Class clazz) {
        return Arrays.asList(clazz.getDeclaredFields())
                .stream()
                .filter(x -> ReflectionHelper.isPublic(x))
                .collect(Collectors.toList());
    }

    public static List<Method> getDeclaredMethodsInternal(Class clazz) {
        List<Method> declaredMethods = Arrays.asList(clazz.getDeclaredMethods())
                .stream()
                .filter(x -> ReflectionHelper.isPublic(x))
                .collect(Collectors.toList());

        if (clazz.isInterface()) {
            return declaredMethods;
        }

        List<Method> result = new ArrayList<>();

        // filter superclass methods with the same signature and return type
        if (clazz != Object.class) {
            Class superclass = clazz.getSuperclass();
            List<Method> superclassMethods = Arrays.asList(superclass.getMethods());

            for (Method method : declaredMethods) {
                boolean ok = true;
                for (Method superclassMethod : superclassMethods)
                    if (ReflectionHelper.getMethodsAreEquivalent(method, superclassMethod)) {
                        ok = false;
                        break;
                    }

                if (ok)
                    result.add(method);
            }
        } else {
            result.addAll(declaredMethods);
        }

        // include missing methods from implemented interfaces
        if (ReflectionHelper.isAbstract(clazz)) {
            Class[] interfaces = clazz.getInterfaces();
            for (Class iface : interfaces) {
                Method[] interfaceMethods = iface.getMethods();
                for (Method interfaceMethod : interfaceMethods) {
                    boolean ok = true;
                    for (Method method : result) {
                        if (ReflectionHelper.getMethodsAreEquivalent(method, interfaceMethod)) {
                            ok = false;
                            break;
                        }
                    }

                    if (ok)
                        result.add(interfaceMethod);
                }
            }
        }

        return result;
    }

    public static List<Constructor> getDeclaredConstructorsInternal(Class clazz) {
        return Arrays.asList(clazz.getDeclaredConstructors())
                .stream()
                .filter(x -> ReflectionHelper.isPublic(x))
                .collect(Collectors.toList());
    }

    //TODO: copy public interfaces from private interface
    public static List<Type> getImplementedInterfacesInternal(Class clazz) {
        // API docs states for both methods: "The order of the interface objects in the array corresponds to the
        // order of the interface names in the implements clause of the declaration of the class"
        Class[] interfaces = clazz.getInterfaces();
        Type[] genericInterfaces = clazz.getGenericInterfaces();

        List<Type> result = new ArrayList<>();
        for (int i = 0; i < interfaces.length; i++) {
            if (ReflectionHelper.isPublic(interfaces[i]))
                result.add(genericInterfaces[i]);
        }

        return result;
    }
}

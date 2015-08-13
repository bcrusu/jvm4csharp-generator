package com.jvm4csharp.generator.reflectx;

import java.util.HashMap;
import java.util.Map;

class XClassFactory {
    private static Map<Class, XClass> _xClassCache = new HashMap<>();

    public static XClass getClass(Class clazz) {
        XClass result;
        if (!_xClassCache.containsKey(clazz)) {
            result = new XClass(new XTypeFactory(), clazz);
            _xClassCache.put(clazz, result);
        } else
            result = _xClassCache.get(clazz);

        return result;
    }
}

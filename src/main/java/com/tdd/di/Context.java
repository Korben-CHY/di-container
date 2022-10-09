package com.tdd.di;

import jakarta.inject.Inject;
import jakarta.inject.Provider;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Context {
    Map<Class<?>, Provider<?>> providers = new HashMap<>();

    public <Type> void bind(Class<Type> type, Type instance) {
        providers.put(type, () -> instance);
    }

    public <Type> Type get(Class<Type> type) {
        return (Type) providers.get(type).get();
    }

    public <Type, Implementation extends Type> void bind(Class<Type> type, Class<Implementation> implement) {
        providers.put(type, () -> {
            try {
                Constructor<Implementation> constructor = getInjectConstructor(implement);
                Object[] dependencies = Arrays.stream(constructor.getParameters())
                        .map(p -> get(p.getType()))
                        .toArray();
                return constructor.newInstance(dependencies);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    private <Type, Implementation extends Type> Constructor<Implementation> getInjectConstructor(Class<Implementation> implement) {
        return (Constructor<Implementation>) Arrays.stream(implement.getConstructors())
                .filter(c -> c.isAnnotationPresent(Inject.class))
                .findFirst()
                .orElseGet(() -> {
                    try {
                        return implement.getConstructor();
                    } catch (NoSuchMethodException e) {
                        throw new RuntimeException();
                    }
                });
    }
}

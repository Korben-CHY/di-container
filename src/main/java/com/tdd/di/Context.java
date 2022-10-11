package com.tdd.di;

import jakarta.inject.Inject;
import jakarta.inject.Provider;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

public class Context {
    Map<Class<?>, Provider<?>> providers = new HashMap<>();

    public <Type> void bind(Class<Type> type, Type instance) {
        providers.put(type, () -> instance);
    }

    public <Type, Implementation extends Type> void bind(Class<Type> type, Class<Implementation> implement) {
        Constructor<Implementation> constructor = getInjectConstructor(implement);
        providers.put(type, () -> {
            try {
                Object[] dependencies = Arrays.stream(constructor.getParameters())
                        .map(p -> get(p.getType()).orElseThrow(DependencyNotFoundException::new))
                        .toArray();
                return constructor.newInstance(dependencies);
            } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private <Type, Implementation extends Type> Constructor<Implementation> getInjectConstructor(Class<Implementation> implement) {
        List<Constructor<?>> injectConstructors = Arrays.stream(implement.getConstructors())
                .filter(c -> c.isAnnotationPresent(Inject.class))
                .collect(Collectors.toList());

        if (injectConstructors.size() > 1) {
            throw new IllegalComponentException();
        }

        return (Constructor<Implementation>) injectConstructors
                .stream()
                .findFirst()
                .orElseGet(() -> {
                    try {
                        return implement.getConstructor();
                    } catch (NoSuchMethodException e) {
                        throw new IllegalComponentException();
                    }
                });
    }

    public <Type> Optional<Type> get(Class<Type> type) {
        Provider<?> provider = providers.get(type);
        return (Optional<Type>) Optional.ofNullable(provider).map(p -> p.get());

    }
}

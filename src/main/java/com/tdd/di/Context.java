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
        providers.put(type, new ConstructorInjectionProvider<>(type, getInjectConstructor(implement)));
    }

    class ConstructorInjectionProvider<Type> implements Provider {
        private Constructor<Type> constructor;
        private Class<?> component;
        private boolean isConstructing;

        public ConstructorInjectionProvider(Class<?> component, Constructor<Type> constructor) {
            this.component = component;
            this.constructor = constructor;
        }

        @Override
        public Object get() {
            if (isConstructing) {
                throw new CyclicDependencyException(this.component);
            }

            try {
                isConstructing = true;
                Object[] dependencies = Arrays.stream(constructor.getParameters())
                        .map(p -> Context.this.get(p.getType()).orElseThrow(() -> new DependencyNotFoundException(p.getType())))
                        .toArray();
                return constructor.newInstance(dependencies);
            } catch (CyclicDependencyException e) {
                throw new CyclicDependencyException(this.component, e);
            } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(e);
            } finally {
                isConstructing = false;
            }
        }
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

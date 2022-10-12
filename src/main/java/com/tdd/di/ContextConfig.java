package com.tdd.di;

import jakarta.inject.Inject;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.stream.Collectors;

public class ContextConfig {
    Map<Class<?>, ComponentProvider<?>> providers = new HashMap<>();
    Map<Class<?>, List<Class<?>>> dependencies = new HashMap<>();

    public <Type> void bind(Class<Type> type, Type instance) {
        providers.put(type, context -> instance);
        dependencies.put(type, List.of());
    }

    public <Type, Implementation extends Type> void bind(Class<Type> type, Class<Implementation> implement) {
        Constructor<Implementation> constructor = getInjectConstructor(implement);
        providers.put(type, new ConstructorInjectionProvider<>(type, constructor));
        dependencies.put(type, Arrays.stream(constructor.getParameters()).map(Parameter::getType).collect(Collectors.toList()));
    }

    public Context getContext() {
        for (Class<?> component : dependencies.keySet()) {
            for (Class<?> dependency : dependencies.get(component)) {
                if (!providers.containsKey(dependency)) {
                    throw new DependencyNotFoundException(dependency);
                }

            }
        }


        return new Context() {
            @Override
            public <Type> Optional<Type> get(Class<Type> type) {
                ComponentProvider<?> provider = providers.get(type);
                return (Optional<Type>) Optional.ofNullable(provider).map(p -> p.get(this));
            }
        };
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

    class ConstructorInjectionProvider<Type> implements ComponentProvider {
        private Constructor<Type> constructor;
        private Class<?> component;
        private boolean isConstructing;

        public ConstructorInjectionProvider(Class<?> component, Constructor<Type> constructor) {
            this.component = component;
            this.constructor = constructor;
        }

        @Override
        public Type get(Context context) {
            if (isConstructing) {
                throw new CyclicDependencyException(this.component);
            }

            try {
                isConstructing = true;
                Object[] dependencies = Arrays.stream(constructor.getParameters())
                        .map(p -> getContext().get(p.getType()).get())
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
}

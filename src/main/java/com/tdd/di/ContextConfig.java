package com.tdd.di;

import jakarta.inject.Inject;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.stream.Collectors;

public class ContextConfig {
    Map<Class<?>, ComponentProvider<?>> providers = new HashMap<>();

    public <Type> void bind(Class<Type> type, Type instance) {
        providers.put(type, new ComponentProvider<>() {
            @Override
            public Object get(Context context) {
                return instance;
            }

            @Override
            public List<Class<?>> getDependencies() {
                return List.of();
            }
        });
    }

    public <Type, Implementation extends Type> void bind(Class<Type> type, Class<Implementation> implement) {
        Constructor<Implementation> constructor = getInjectConstructor(implement);
        providers.put(type, new ConstructorInjectionProvider<>(constructor));
    }

    public Context getContext() {
        providers.keySet().forEach(component -> checkDependencies(component, new Stack<>()));

        return new Context() {
            @Override
            public <Type> Optional<Type> get(Class<Type> type) {
                ComponentProvider<?> provider = providers.get(type);
                return (Optional<Type>) Optional.ofNullable(provider).map(p -> p.get(this));
            }
        };
    }

    private void checkDependencies(Class<?> component, Stack<Class<?>> visiting) {
        for (Class<?> dependency : providers.get(component).getDependencies()) {
            if (!providers.containsKey(dependency)) {
                throw new DependencyNotFoundException(dependency);
            }

            if (visiting.contains(dependency)) {
                throw new CyclicDependencyException(visiting);
            }
            visiting.push(dependency);

            checkDependencies(dependency, visiting);

            visiting.pop();
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

    class ConstructorInjectionProvider<Type> implements ComponentProvider {
        private Constructor<Type> constructor;
        private final List<Class<?>> dependencies;

        public ConstructorInjectionProvider(Constructor<Type> constructor) {
            this.constructor = constructor;

            dependencies = Arrays.stream(constructor.getParameters())
                    .map(Parameter::getType).collect(Collectors.toList());
        }

        @Override
        public Type get(Context context) {
            try {
                Object[] dependencies = Arrays.stream(constructor.getParameters())
                        .map(p -> getContext().get(p.getType()).get())
                        .toArray();
                return constructor.newInstance(dependencies);
            } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public List<Class<?>> getDependencies() {
            return dependencies;
        }

    }
}

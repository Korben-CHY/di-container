package com.tdd.di;

import jakarta.inject.Inject;

import java.lang.reflect.Constructor;
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

    public <Type, Implementation extends Type> void bind(Class<Type> type, Class<Implementation> implementation) {
        providers.put(type, new ConstructorInjectionProvider<>(implementation));
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

}

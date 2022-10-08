package com.tdd.di;

import jakarta.inject.Provider;

import java.util.HashMap;
import java.util.Map;

public class Context {
    Map<Class<?>, Provider<?>> providers = new HashMap<>();

    public <ComponentType> void bind(Class<ComponentType> type, ComponentType instance) {
        providers.put(type, () -> instance);
    }

    public <ComponentType> ComponentType get(Class<ComponentType> type) {
        return (ComponentType) providers.get(type).get();
    }

    public <ComponentType, ComponentImplement extends ComponentType> void bind(Class<ComponentType> type, Class<ComponentImplement> implement) {
        providers.put(type, () -> {
            try {
                return implement.getConstructor().newInstance();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
}

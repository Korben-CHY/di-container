package com.tdd.di;

import java.util.HashSet;
import java.util.Set;

public class CyclicDependencyException extends RuntimeException {
    private Set<Class<?>> components = new HashSet<>();

    public CyclicDependencyException(Class<?> component) {
        this.components.add(component);
    }

    public CyclicDependencyException(Class<?> component, CyclicDependencyException e) {
        this.components.add(component);
        this.components.addAll(e.getComponent());
    }

    public Set<Class<?>> getComponent() {
        return components;
    }
}

package com.tdd.di;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

public class CyclicDependencyException extends RuntimeException {
    private Set<Class<?>> components = new HashSet<>();

    public CyclicDependencyException(Stack<Class<?>> visiting) {
        this.components.addAll(visiting);
    }

    public Set<Class<?>> getComponent() {
        return components;
    }
}

package com.tdd.di;

import java.util.Stack;

public class CyclicDependenciesFoundException extends RuntimeException {

    private Class<?>[] components;

    public CyclicDependenciesFoundException(Stack<Class<?>> visiting) {
        components = new Class<?>[visiting.size()];
        int size = visiting.size();
        for (int i = 0; i < size; i++) {
            components[i] = visiting.pop();
        }
    }

    public Class<?>[] getComponents() {
        return components;
    }
}

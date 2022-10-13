package com.tdd.di;

import java.util.List;

public interface ComponentProvider<T> {
    T get(Context context);
    List<Class<?>> getDependencies();
}

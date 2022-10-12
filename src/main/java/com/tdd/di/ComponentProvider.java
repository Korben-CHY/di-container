package com.tdd.di;

public interface ComponentProvider<T> {
    T get(Context context);
}

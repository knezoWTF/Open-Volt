package com.volt.structure;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class Registry<T> {

    private final List<T> list;

    private Consumer<T> addConsumer;
    private Consumer<T> removeConsumer;

    public Registry(final Supplier<List<T>> list) {
        this.list = list.get();
    }

    public Registry() {
        this(CopyOnWriteArrayList::new);
    }

    public abstract void init();

    @SafeVarargs
    public final void add(final T... t) {
        for (final T t1 : t) {
            this.addBy(t1, list.size());
        }
    }

    @SafeVarargs
    public final void remove(final T... t) {
        for (final T t1 : t) {
            this.list.remove(t1);

            if (Objects.nonNull(this.removeConsumer)) {
                this.removeConsumer.accept(t1);
            }
        }
    }

    public <V extends T> V byClass(final Class<V> clazz) {
        //noinspection unchecked
        return (V) this.list.stream()
                .filter(t -> t.getClass() == clazz)
                .findFirst()
                .orElse(null);
    }

    public void addBy(final T t, final int index) {
        if (list.contains(t))
            return;

        this.list.add(index, t);
        if (Objects.nonNull(this.addConsumer)) {
            this.addConsumer.accept(t);
        }
    }

    public void removeBy(final int index) {
        this.list.remove(index);
    }

    public List<T> list() {
        return this.list;
    }

    public void addConsumer(final Consumer<T> addConsumer) {
        this.addConsumer = addConsumer;
    }

    public void removeConsumer(final Consumer<T> removeConsumer) {
        this.removeConsumer = removeConsumer;
    }
}

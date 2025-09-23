package me.thedivazo.zombiecouriers.util;

import lombok.Getter;

import java.util.Map;
import java.util.Objects;

@Getter
public final class Pair<T, V> implements Map.Entry<T, V> {
    private final T first;
    private final V second;

    public Pair(T first, V second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public T getKey() {
        return first;
    }

    @Override
    public V getValue() {
        return second;
    }

    @Override
    public V setValue(V value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Pair)) return false;

        @SuppressWarnings("unchecked")
        Pair<T, V> pair = (Pair<T, V>) o;
        return Objects.equals(first, pair.first) && Objects.equals(second, pair.second);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(first);
        result = 31 * result + Objects.hashCode(second);
        return result;
    }
}

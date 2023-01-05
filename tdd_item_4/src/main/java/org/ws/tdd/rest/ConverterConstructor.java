package org.ws.tdd.rest;

import java.lang.reflect.InvocationTargetException;
import java.util.Optional;

public class ConverterConstructor {
    public static Optional<Object> convert(Class<?> converter, String value) {
        try {
            return Optional.of(converter.getConstructor(String.class).newInstance(value));
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            return Optional.empty();
        }
    }
}

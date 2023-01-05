package org.ws.tdd.rest;

import java.lang.reflect.InvocationTargetException;
import java.util.Optional;

public class ConverterFactory {
    public static Optional<Object> convert(Class<?> converter, String value) {
        try {
            return Optional.of(converter.getMethod("valueOf", String.class).invoke(null, value));
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            return Optional.empty();
        }
    }
}

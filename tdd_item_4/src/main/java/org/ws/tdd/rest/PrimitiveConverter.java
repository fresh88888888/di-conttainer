package org.ws.tdd.rest;

import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.ws.tdd.rest.MethodInvoker.ValueConverter.singleValued;

public class PrimitiveConverter {
    private static Map<Type, MethodInvoker.ValueConverter<Object>> primitives = Map.of(
            int.class, singleValued(Integer::parseInt),
            double.class, singleValued(Double::parseDouble),
            short.class, singleValued(Short::parseShort),
            byte.class, singleValued(Byte::parseByte),
            boolean.class, singleValued(Boolean::parseBoolean),
            String.class, singleValued(s -> s)
    );
    public static Optional<Object> converter(Parameter parameter, List<String> values) {
        return Optional.ofNullable(primitives.get(parameter.getType())).map(c -> c.fromString(values));
    }
}

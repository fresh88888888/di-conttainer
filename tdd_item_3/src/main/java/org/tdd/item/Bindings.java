package org.tdd.item;

import jakarta.inject.Qualifier;
import jakarta.inject.Scope;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.tdd.item.ContextConfigException.illegalAnnotation;

public class Bindings {
    Class<?> type;
    Map<Class<?>, List<Annotation>> group;

    public Bindings(Class<?> type, Annotation[] annotations, Class<? extends Annotation>... allowed) {
        this.type = type;
        this.group = parse(type, annotations, allowed);
    }
    Optional<Annotation> scope() {
        List<Annotation> scopes = group.getOrDefault(Scope.class, from(type, Scope.class));
        if (scopes.size() > 1) {
            throw illegalAnnotation(type, scopes);
        }
        return scopes.stream().findFirst();
    }

    public static Bindings component(Class<?> component, Annotation... annotations) {
        return new Bindings(component, annotations, Qualifier.class, Scope.class);
    }

    public static Bindings instance(Class<?> component, Annotation... annotations) {
        return new Bindings(component, annotations, Qualifier.class);
    }

    static Function<Annotation, Class<?>> allow(Class<? extends Annotation>... annotations) {
        return annotation -> Stream.of(annotations).filter(annotation.annotationType()::isAnnotationPresent).findFirst().orElse(Illegal.class);
    }

    private static Map<Class<?>, List<Annotation>> parse(Class<?> type, Annotation[] annotations, Class<? extends Annotation>... allowed) {
        Map<Class<?>, List<Annotation>> annotationGroups = Arrays.stream(annotations).collect(Collectors.groupingBy(allow(allowed), Collectors.toList()));

        if (annotationGroups.containsKey(Illegal.class)) {
            throw illegalAnnotation(type, annotationGroups.get(Illegal.class));
        }
        return annotationGroups;
    }

    private static List<Annotation> from(Class<?> implementation, Class<? extends Annotation> annotation) {
        return Arrays.stream(implementation.getAnnotations()).filter(a -> a.annotationType().isAnnotationPresent(annotation)).toList();
    }

    List<Annotation> qualifiers() {
        return group.getOrDefault(Qualifier.class, List.of());
    }

    private @interface Illegal {
    }
}

package org.tdd.item;

import jakarta.inject.Qualifier;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.List;
import java.util.Optional;

import static java.util.Arrays.stream;
import static org.tdd.item.ComponentError.ambiguousQualifiers;

record Injectable<Element extends AccessibleObject>(Element element, ComponentRef<?>[] required) {
    Object[] toDependencies(Context context) {
        return stream(required).map(context::get).map(Optional::get).toArray();
    }
    static <Element extends Executable> Injectable<Element> of(Element element) {
        element.setAccessible(true);
        return new Injectable<>(element, stream(element.getParameters()).map(Injectable::toComponentRef).toArray(ComponentRef<?>[]::new));
    }

    static Injectable<Field> of(Field field) {
        field.setAccessible(true);
        return new Injectable<>(field, new ComponentRef<?>[]{toComponentRef(field)});
    }

    static ComponentRef toComponentRef(Field field) {
        return ComponentRef.of(field.getGenericType(), getQualifier(field));
    }

    static ComponentRef<?> toComponentRef(Parameter p) {
        return ComponentRef.of(p.getParameterizedType(), getQualifier(p));
    }

    static Annotation getQualifier(AnnotatedElement element) {
        List<Annotation> qualifiers = stream(element.getAnnotations()).filter(a -> a.annotationType().isAnnotationPresent(Qualifier.class)).toList();
        if (qualifiers.size() > 1) {
            throw ambiguousQualifiers(element, qualifiers);
        }
        return qualifiers.stream().findFirst().orElse(null);
    }
}

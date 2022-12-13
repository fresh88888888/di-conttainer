package org.tdd.item;

import java.lang.annotation.Annotation;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class ContextConfigException extends RuntimeException {

    public ContextConfigException(String message) {
        super(message);
    }

    static ContextConfigException illegalAnnotation(Class<?> type, List<Annotation> annotations) {
        return new ContextConfigException(MessageFormat.format("UnQualified annotations: {0} of {1}", String.join(",", annotations.stream().map(Object::toString).toList()), type));
    }

    static ContextConfigException unknownScope(Class<? extends Annotation> annotationType) {
        return new ContextConfigException(MessageFormat.format("Unknown scope: {0}", annotationType));
    }

    static ContextConfigException duplicated(Component component) {
        return new ContextConfigException(MessageFormat.format("Duplicated: {0}", component));
    }
}

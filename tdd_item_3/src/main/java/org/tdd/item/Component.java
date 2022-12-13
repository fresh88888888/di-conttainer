package org.tdd.item;

import java.lang.annotation.Annotation;

public record Component(Class<?> type, Annotation qualifier) {
}

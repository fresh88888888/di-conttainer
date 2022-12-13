package org.tdd.item;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.Objects;

import static java.util.stream.Collectors.joining;

class ContextConfigError extends Error {
    public static ContextConfigError unsatisfiedResolution(Component component, Component dependency) {
        return new ContextConfigError(MessageFormat.format("Unsatisfied resolution: {1} for {0} ", component, dependency));
    }

    public static ContextConfigError circularDependencies(Collection<Component> path, Component circular) {
        return new ContextConfigError(MessageFormat.format("Circular dependencies: {0} -> [{1}]",
                path.stream().map(Objects::toString).collect(joining(" -> ")), circular));
    }

    ContextConfigError(String message) {
        super(message);
    }
}

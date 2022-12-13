package org.tdd.item;

interface ScopeProvider {
    ComponentProvider<?> create(ComponentProvider<?> provider);
}

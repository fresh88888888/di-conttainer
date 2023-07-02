package org.tdd.item;

import jakarta.inject.Provider;
import jakarta.inject.Singleton;

import java.lang.annotation.*;
import java.lang.reflect.Field;
import java.util.*;

import static org.tdd.item.ContextConfigError.circularDependencies;
import static org.tdd.item.ContextConfigError.unsatisfiedResolution;
import static org.tdd.item.ContextConfigException.*;
import static org.tdd.item.ContextConfigException.unknownScope;

public class ContextConfig {
    private final Map<Component, ComponentProvider<?>> components = new HashMap<>();
    private final Map<Class<?>, ScopeProvider> scopes = new HashMap<>();

    public ContextConfig() {
        scopes.put(Singleton.class, SingletonProvider::new);
    }
    public <Type> void instance(Class<Type> type, Type instance) {
        bind(new Component(type, null), context -> instance);
    }
    public <Type> void instance(Class<Type> type, Type instance, Annotation... annotations) {
        bindInstance(type, instance, annotations);
    }
    public <Type, Implementation extends Type> void component(Class<Type> type, Class<Implementation> implementation, Annotation... annotations) {
       bindComponent(type, implementation, annotations);
    }
    public <ScopeType extends Annotation>void scope(Class<ScopeType> scope, ScopeProvider provider) {
        scopes.put(scope, provider);
    }
    public void from(Config config) {
        new DSL(config).bind();
    }
    public Context getContext() {
        components.keySet().forEach(component -> checkDependencies(component, new Stack<>()));
        Map<Component, ComponentProvider<?>> context = new HashMap<>(components);
        return new Context() {
            @Override
            public <ComponentType> Optional<ComponentType> get(ComponentRef<ComponentType> ref) {
                if (ref.isContainer()) {
                    if (ref.getContainer() != Provider.class) {
                        return Optional.empty();
                    }

                    return (Optional<ComponentType>) Optional.ofNullable(getProvider(ref)).map(provider -> (Provider<Object>) () -> provider.get(this));
                }

                return Optional.ofNullable(getProvider(ref)).map(provider -> (ComponentType) provider.get(this));
            }
            private  <ComponentType> ComponentProvider<?> getProvider(ComponentRef<ComponentType> ref) {
                return context.get(ref.component());
            }
        };
    }
    private void bindComponent(Class<?> type, Class<?> implementation, Annotation... annotations) {
        Bindings bindings = Bindings.component(implementation, annotations);
        bind(type, bindings.qualifiers(), provider(implementation, bindings.scope()));
    }
    private  void bindInstance(Class<?> type, Object instance, Annotation[] annotations) {
        bind(type, Bindings.instance(type, annotations).qualifiers(), context -> instance);
    }
    private <Type> void bind(Class<Type> type, List<Annotation> qualifiers, ComponentProvider<?> provider) {
        if (qualifiers.isEmpty()) {
            bind(new Component(type, null), provider);
        }
        for (Annotation qualifier : qualifiers) {
            bind(new Component(type, qualifier), provider);
        }
    }
    private void bind(Component component, ComponentProvider<?> provider) {
        if(components.containsKey(component)){
            throw duplicated(component);
        }
        components.put(component, provider);
    }

    private <Type> ComponentProvider<?> provider(Class<Type> implementation, Optional<Annotation> scope){
        ComponentProvider<?> injectionProvider = new InjectionProvider<>(implementation);
        return scope.<ComponentProvider<?>>map(s -> scoped(s, injectionProvider)).orElse(injectionProvider);
    }
    private ComponentProvider<?> scoped(Annotation scope, ComponentProvider<?> provider) {
        if (!scopes.containsKey(scope.annotationType()))
            throw unknownScope(scope.annotationType());
        return scopes.get(scope.annotationType()).create(provider);
    }
    private void checkDependencies(Component component, Stack<Component> visiting) {
        for (ComponentRef<?> dependency : components.get(component).getDependencies()) {
            if (!components.containsKey(dependency.component())) {
                throw unsatisfiedResolution(component, dependency.component());
            }
            if (!dependency.isContainer()) {
                if (visiting.contains(dependency.component())) {
                    throw circularDependencies(visiting, dependency.component());
                }

                visiting.push(dependency.component());
                checkDependencies(dependency.component(), visiting);
                visiting.pop();
            }
        }
    }
    class DSL{
        private Config config;

        public DSL(Config config) {
            this.config = config;
        }
        void bind(){
            for (Declaration declaration :declarations()) {
                declaration.value().ifPresentOrElse(declaration::bindInstance, declaration::bindComponent);
            }
        }
        private List<Declaration> declarations(){
           return Arrays.stream(config.getClass().getDeclaredFields()).filter(f-> !f.isSynthetic()).map(Declaration::new).toList();
        }
        class Declaration{
            private Field field;

            public Declaration(Field field) {
                this.field = field;
            }
            void bindInstance(Object instance){
                ContextConfig.this.bindInstance(type(), instance, annotations());
            }
            void bindComponent(){
                ContextConfig.this.bindComponent(type(), field.getType(), annotations());
            }
            private Optional<Object> value(){
                try {
                    field.setAccessible(true);
                    return Optional.ofNullable(field.get(config));
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
            private Class<?> type(){
                Config.Export export = field.getAnnotation(Config.Export.class);
                return export != null ? export.value() : field.getType();
            }
            private Annotation[] annotations(){
                return Arrays.stream(field.getAnnotations()).filter(a-> a.annotationType() != Config.Export.class).toArray(Annotation[]::new);
            }
        }
    }
}



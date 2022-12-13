package org.tdd.item;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.inject.Singleton;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.awt.*;
import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.Assert.*;
import static org.tdd.item.PooledProvider.*;

@Nested
public class ContextTest {
    private ContextConfig config;

    @BeforeEach
    public void setup() {
        config = new ContextConfig();
    }

    @Nested
    class DependencyChecked {

        @ParameterizedTest
        @MethodSource
        public void should_throw_exception_if_dependency_not_found(Class<? extends TestComponent> componentType) {
            config.component(TestComponent.class, componentType);
            ContextConfigError exception = assertThrows(ContextConfigError.class, () -> config.getContext());
            assertNotNull(exception);
        }

        public static Stream<Arguments> should_throw_exception_if_dependency_not_found(){
            return Stream.of(
                    Arguments.of(Named.of("Inject Constructor", MissingDependencyConstructor.class)),
                    Arguments.of(Named.of("Inject Field", MissingDependencyField.class)),
                    Arguments.of(Named.of("Inject Method", MissingDependencyMethod.class)),
                    Arguments.of(Named.of("Provider in Inject Constructor", MissingDependencyProviderConstructor.class)),
                    Arguments.of(Named.of("Provider in Inject Field", MissingDependencyProviderField.class)),
                    Arguments.of(Named.of("Provider in Inject Method", MissingDependencyProviderMethod.class)),
                    Arguments.of(Named.of("Scope  in Inject Field", MissingDependencyScoped.class)),
                    Arguments.of(Named.of("Scope Provider in Inject Field", MissingDependencyProviderScoped.class))
            );
        }
        static class MissingDependencyConstructor implements TestComponent {
            @Inject
            public MissingDependencyConstructor(Dependency dependency) {
            }
        }
        static class MissingDependencyField implements TestComponent {
            @Inject
            Dependency dependency;
        }
        static class MissingDependencyMethod implements TestComponent {
            @Inject
            public void install(Dependency dependency){
            }
        }
        static class MissingDependencyProviderConstructor implements TestComponent {
            @Inject
            public MissingDependencyProviderConstructor(Provider<Dependency> dependencyProvider) {
            }
        }
        static class MissingDependencyProviderField implements TestComponent {
            @Inject
            Provider<Dependency> dependency;
        }
        static class MissingDependencyProviderMethod implements TestComponent {
            @Inject
            public void install(Provider<Dependency> dependency){
            }
        }
        @Singleton
        static class MissingDependencyScoped implements TestComponent{
            @Inject
            Dependency dependency;
        }
        @Singleton
        static class MissingDependencyProviderScoped implements TestComponent{
            @Inject
            Provider<Dependency> dependency;
        }

        @ParameterizedTest(name = "cyclic dependency between {0} and {1}")
        @MethodSource
        public void should_throw_exception_if_cyclic_dependencies_found(Class<? extends TestComponent> component, Class<? extends Dependency> dependency) {
            config.component(TestComponent.class, component);
            config.component(Dependency.class, dependency);
            assertThrows(ContextConfigError.class, () -> config.getContext());
        }
        public static Stream<Arguments> should_throw_exception_if_cyclic_dependencies_found(){
            List<Arguments> arguments = new ArrayList<>();
            for (Named component:List.of(
                    Named.of("Inject constructor", CyclicComponentInjectConstructor.class),
                    Named.of("Inject Field", CyclicComponentInjectField.class),
                    Named.of("Inject Method", CyclicComponentInjectMethod.class)
            )) {
                for (Named dependency:List.of(
                        Named.of("Inject Constructor", CyclicDependencyInjectConstructor.class),
                        Named.of("Inject Field", CyclicDependencyInjectField.class),
                        Named.of("Inject Method", CyclicDependencyInjectMethod.class)
                )) {
                    arguments.add(Arguments.of(component, dependency));
                }
            }
            return arguments.stream();
        }
        static class CyclicComponentInjectConstructor implements TestComponent {
            @Inject
            public CyclicComponentInjectConstructor(Dependency dependency) {
            }
        }
        static class CyclicComponentInjectField implements TestComponent {
            @Inject
            Dependency dependency;
        }
        static class CyclicComponentInjectMethod implements TestComponent {
            @Inject
            public void install(Dependency dependency){
            }
        }
        static class CyclicDependencyInjectConstructor implements Dependency{
            @Inject
            public CyclicDependencyInjectConstructor(TestComponent component) {
            }
        }
        static class CyclicDependencyInjectField implements Dependency{
            @Inject
            TestComponent component;
        }
        static class CyclicDependencyInjectMethod implements Dependency{
            @Inject
            public void install(TestComponent component){
            }
        }

        @ParameterizedTest(name = "indirect cyclic dependency between {0} {1} and {2}")
        @MethodSource
        public void should_throw_exception_if_transitive_cyclic_dependencies_found(Class<? extends TestComponent> component,
                                                                                   Class<? extends Dependency> dependency,
                                                                                   Class<? extends AnotherDependency> anotherDependency) {
            config.component(TestComponent.class, component);
            config.component(Dependency.class, dependency);
            config.component(AnotherDependency.class, anotherDependency);
            ContextConfigError exception = assertThrows(ContextConfigError.class, () -> config.getContext());
            Assertions.assertNotNull(exception);
        }

        public static Stream<Arguments> should_throw_exception_if_transitive_cyclic_dependencies_found(){
            List<Arguments> arguments = new ArrayList<>();
            for (Named component:List.of(
                    Named.of("Inject constructor", CyclicComponentInjectConstructor.class),
                    Named.of("Inject Field", CyclicComponentInjectField.class),
                    Named.of("Inject Method", CyclicComponentInjectMethod.class)
            )) {
                for (Named dependency:List.of(
                        Named.of("Inject Constructor", IndirectCyclicDependencyInjectConstructor.class),
                        Named.of("Inject Field", IndirectCyclicDependencyInjectField.class),
                        Named.of("Inject Method", IndirectCyclicDependencyInjectMethod.class)
                )) {
                    for (Named anotherDependency:List.of(
                            Named.of("Inject Constructor", IndirectCyclicAnotherDependencyInjectConstructor.class),
                            Named.of("Inject Field", IndirectCyclicAnotherDependencyInjectField.class),
                            Named.of("Inject Method", IndirectCyclicAnotherDependencyInjectMethod.class)
                    )) {
                        arguments.add(Arguments.of(component, dependency, anotherDependency));
                    }
                }
            }
            return arguments.stream();
        }

        static class IndirectCyclicDependencyInjectConstructor implements Dependency{
            @Inject
            public IndirectCyclicDependencyInjectConstructor(AnotherDependency anotherDependency) {
            }
        }
        static class IndirectCyclicDependencyInjectField implements Dependency{
            @Inject
            AnotherDependency anotherDependency;
        }
        static class IndirectCyclicDependencyInjectMethod implements Dependency{
            @Inject
            public void install(AnotherDependency anotherDependency){
            }
        }
        static class IndirectCyclicAnotherDependencyInjectConstructor implements TestComponent {
            @Inject
            public IndirectCyclicAnotherDependencyInjectConstructor(TestComponent component) {
            }
        }
        static class IndirectCyclicAnotherDependencyInjectField implements TestComponent {
            @Inject
            TestComponent component;
        }
        static class IndirectCyclicAnotherDependencyInjectMethod implements TestComponent {
            @Inject
            public void install(TestComponent component){
            }
        }
        static class CyclicDependencyProviderInjectConstructor implements Dependency{
            @Inject
            public CyclicDependencyProviderInjectConstructor(Provider<TestComponent> component) {
            }
        }
        @Test
        public void should_not_throw_exception_if_cyclic_dependency_via_provider(){
            config.component(TestComponent.class, CyclicComponentInjectConstructor.class);
            config.component(Dependency.class, CyclicDependencyProviderInjectConstructor.class);

            assertTrue(config.getContext().get(ComponentRef.of(TestComponent.class)).isPresent());
            assertTrue(config.getContext().get(ComponentRef.of(Dependency.class)).isPresent());
        }

        @Nested
        public class WithQualifier{

            @ParameterizedTest(name = "{1} -> @SkyWater({0}) -> @Named(\"ChosenOne\") not cyclic dependencies")
            @MethodSource
            public void should_not_throw_cyclic_exception_if_component_with_same_type_tagged_with_different_qualifier(Class<? extends Dependency> skyWater,
                                                                                                                      Class<? extends Dependency> noCyclic){
                Dependency dependency = new Dependency() {};
                config.instance(Dependency.class, dependency, new NameLiteral("ChosenOne"));
                config.component(Dependency.class, skyWater, new SkyWaterLiteral());
                config.component(Dependency.class, noCyclic);

                Assertions.assertDoesNotThrow(()->config.getContext());
            }
            public static Stream<Arguments> should_not_throw_cyclic_exception_if_component_with_same_type_tagged_with_different_qualifier() {
                List<Arguments> arguments = new ArrayList<>();
                for (Named skyWater : List.of(
                        Named.of("Inject constructor Qualifier", SkyWaterInjectConstructor.class),
                        Named.of("Inject Field Qualifier", SkyWaterInjectField.class),
                        Named.of("Inject Method Qualifier", SkyWaterInjectMethod.class)
                )) {
                    for (Named noCyclic : List.of(
                            Named.of("Inject Constructor Qualifier", NotCyclicInjectConstructor.class),
                            Named.of("Inject Field Qualifier", NotCyclicInjectField.class),
                            Named.of("Inject Method Qualifier", NotCyclicInjectMethod.class)
                    )) {
                        arguments.add(Arguments.of(skyWater, noCyclic));
                    }
                }
                return arguments.stream();
            }
            static class SkyWaterInjectConstructor implements Dependency{
                @Inject
                public SkyWaterInjectConstructor(@jakarta.inject.Named("ChosenOne") Dependency dependency) {
                }
            }
            static class SkyWaterInjectField implements Dependency{
                @Inject
                @jakarta.inject.Named("ChosenOne") Dependency dependency;
            }
            static class SkyWaterInjectMethod implements Dependency{
                @Inject
                void install(@jakarta.inject.Named("ChosenOne") Dependency dependency) {
                }
            }
            static class NotCyclicInjectConstructor implements Dependency{
                @Inject
                public NotCyclicInjectConstructor(@SkyWater Dependency dependency) {
                }
            }
            static class NotCyclicInjectField implements Dependency{
                @Inject
                @SkyWater Dependency dependency;
            }
            static class NotCyclicInjectMethod implements Dependency{
                @Inject
                void install(@SkyWater Dependency dependency) {
                }
            }

            @ParameterizedTest
            @MethodSource
            public void should_throw_exception_if_dependency_with_qualifier_not_found(Class<? extends TestComponent> component) {
                Dependency dependency = new Dependency() {};
                config.instance(Dependency.class, dependency);
                config.component(TestComponent.class, component, new NameLiteral("Owner"));
                ContextConfigError exception = assertThrows(ContextConfigError.class, () -> config.getContext());
                Assertions.assertNotNull(exception);
            }
            public static Stream<Arguments> should_throw_exception_if_dependency_with_qualifier_not_found(){
                return Stream.of(
                        Arguments.of(Named.of("Inject Constructor Qualifier", InjectConstructor.class)),
                        Arguments.of(Named.of("Inject Field Qualifier", InjectField.class)),
                        Arguments.of(Named.of("Inject Method Qualifier", InjectMethod.class)),
                        Arguments.of(Named.of("Provider in Inject Constructor Qualifier", InjectConstructorProvider.class)),
                        Arguments.of(Named.of("Provider in Inject Field Qualifier", InjectFieldProvider.class)),
                        Arguments.of(Named.of("Provider in Inject Method Qualifier", InjectMethodProvider.class))
                );
            }
            static class InjectConstructor implements TestComponent{
                @Inject
                public InjectConstructor(@SkyWater Dependency dependency) {
                }
            }
            static class InjectField implements TestComponent{
                @Inject
                @SkyWater Dependency dependency;
            }
            static class InjectMethod implements TestComponent{
                @Inject
                void install(@SkyWater Dependency dependency) {
                }
            }
            static class InjectConstructorProvider implements TestComponent{
                @Inject
                public InjectConstructorProvider(@SkyWater Provider<Dependency>  dependency) {
                }
            }
            static class InjectFieldProvider implements TestComponent{
                @Inject
                @SkyWater Provider<Dependency>  dependency;
            }
            static class InjectMethodProvider implements TestComponent{
                @Inject
                void install(@SkyWater Provider<Dependency>  dependency) {
                }
            }
        }
    }

    @Nested
    class TypeBinding {
        @Test
        public void should_bind_type_to_a_specific_instance() {
            TestComponent instance = new TestComponent() {};
            config.instance(TestComponent.class, instance);

            assertSame(instance, config.getContext().get(ComponentRef.of(TestComponent.class)).get());
        }

        @Test
        public void should_retrieve_empty_for_unbind_type() {
            Optional<TestComponent> component = config.getContext().get(ComponentRef.of(TestComponent.class));
            assertTrue(component.isEmpty());
        }

        @ParameterizedTest(name = "supporting {0}")
        @MethodSource
        public void should_bind_type_to_an_injection_component(Class<? extends TestComponent> componentType){
            Dependency dependency = new Dependency() {};

            config.component(TestComponent.class,componentType);
            config.instance(Dependency.class, dependency);

            Optional<TestComponent> component = config.getContext().get(ComponentRef.of(TestComponent.class));
            assertTrue(component.isPresent());
            assertSame(dependency, component.get().dependency());
        }

        public static Stream<Arguments> should_bind_type_to_an_injection_component(){
            return Stream.of(
                    Arguments.of(Named.of("Constructor Injection",ConstructorInjection.class)),
                    Arguments.of(Named.of("Field Injection",FieldInjection.class)),
                    Arguments.of(Named.of("Method Injection",MethodInjection.class))
            );
        }

        @Test
        public void should_retrieve_bind_type_as_provider(){
            TestComponent instance = new TestComponent() {};
            config.instance(TestComponent.class, instance);

            ParameterizedType type = new TypeLiteral<Provider<TestComponent>>(){}.getType();
            Provider<TestComponent> provider = config.getContext().get(new ComponentRef<Provider<TestComponent>>(){}).get();
            assertSame(instance, provider.get());

        }
        @Test
        public void should_not_retrieve_bind_type_as_unsupported_container(){
            TestComponent instance = new TestComponent() {};
            config.instance(TestComponent.class, instance);

            assertFalse(config.getContext().get(new ComponentRef<List<TestComponent>>(){}).isPresent());
        }
        static abstract class TypeLiteral<T>{
            public ParameterizedType getType(){
                return (ParameterizedType) ((ParameterizedType)this.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
            }
        }
        static class ConstructorInjection implements TestComponent {
            private Dependency dependency;
            @Inject
            public ConstructorInjection(Dependency dependency) {
                this.dependency = dependency;
            }
            @Override
            public Dependency dependency(){
                return dependency;
            }
        }
        static class FieldInjection implements TestComponent {
            @Inject
            Dependency dependency;
            @Override
            public Dependency dependency(){
                return dependency;
            }
        }
        static class MethodInjection implements TestComponent {
            private Dependency dependency;

            @Inject
            public void install(Dependency dependency){
                this.dependency = dependency;
            }
            @Override
            public Dependency dependency(){
                return dependency;
            }
        }

        @Nested
        public class WithQualifier{
            @BeforeEach
            public void setUp(){

            }
            static class InjectConstructor{
                private Dependency dependency;

                @Inject
                public InjectConstructor(Dependency dependency) {
                    this.dependency = dependency;
                }
            }
            @Test
            public void should_bind_instance_with_multi_qualifier(){
                TestComponent instance = new TestComponent() {};
                config.instance(TestComponent.class, instance, new NameLiteral("ChosenOne"), new SkyWaterLiteral());

                TestComponent chosenOne = config.getContext().get(ComponentRef.of(TestComponent.class, new NameLiteral("ChosenOne"))).get();
                TestComponent chosenTwo = config.getContext().get(ComponentRef.of(TestComponent.class, new SkyWaterLiteral())).get();

                assertSame(instance, chosenOne);
                assertSame(instance, chosenTwo);
            }
            @Test
            public void should_bind_component_with_multi_qualifier(){
                Dependency dependency = new Dependency() {};
                config.instance(Dependency.class, dependency);
                config.component(InjectConstructor.class, InjectConstructor.class, new NameLiteral("ChosenOne"), new SkyWaterLiteral());

                InjectConstructor chosenOne = config.getContext().get(ComponentRef.of(InjectConstructor.class, new NameLiteral("ChosenOne"))).get();
                InjectConstructor chosenOTwo = config.getContext().get(ComponentRef.of(InjectConstructor.class, new SkyWaterLiteral())).get();

                assertSame(dependency, chosenOne.dependency);
                assertSame(dependency, chosenOTwo.dependency);
            }
            @Test
            public void should_throw_exception_if_illegal_qualifier_given_to_instance(){
                TestComponent instance = new TestComponent() {};
                assertThrows(ContextConfigException.class, ()-> config.instance(TestComponent.class,instance,  new TestLiteral()));
            }
            @Test
            public void should_throw_exception_if_illegal_qualifier_given_to_component(){
                Dependency dependency = new Dependency() {};
                config.instance(Dependency.class, dependency);
                assertThrows(ContextConfigException.class, ()->  config.component(InjectConstructor.class, InjectConstructor.class, new TestLiteral()));
            }
            @Test
            @Disabled
            public void should_retrieve_bind_type_provider(){
                TestComponent instance = new TestComponent() {};
                config.instance(TestComponent.class, instance, new NameLiteral("ChosenOne"), new SkyWaterLiteral());
                Optional<Provider<TestComponent>> provider = config.getContext().get(new ComponentRef<>(new SkyWaterLiteral()));
                assertTrue(provider.isPresent());
            }
            @Test
            public void should_retrieve_empty_if_no_matched_qualifier(){
                TestComponent instance = new TestComponent() {};
                Optional<TextComponent> component = config.getContext().get(ComponentRef.of(TextComponent.class, new SkyWaterLiteral()));
                assertTrue(component.isEmpty());
            }
        }
        @Nested
        public class WithScope{
            static class NotSingleton{
            }
            @Test
            public void should_not_be_singleton_scope_by_default(){
                config.component(NotSingleton.class, NotSingleton.class);
                Assertions.assertNotSame(config.getContext().get(ComponentRef.of(NotSingleton.class)).get(), config.getContext().get(ComponentRef.of(NotSingleton.class)).get());
            }
            @Test
            public void should_bind_component_as_singleton_scoped(){
                config.component(NotSingleton.class, NotSingleton.class, new SingletonLiteral());
                Assertions.assertSame(config.getContext().get(ComponentRef.of(NotSingleton.class)).get(), config.getContext().get(ComponentRef.of(NotSingleton.class)).get());
            }
            @Singleton
            static class SingletonAnnotated implements Dependency{
            }
            @Test
            public void should_retrieve_scope_annotation_from_component(){
                config.component(Dependency.class, SingletonAnnotated.class);
                assertSame(config.getContext().get(ComponentRef.of(Dependency.class)).get(), config.getContext().get(ComponentRef.of(Dependency.class)).get());
            }
            @Test
            public void should_bind_component_as_customized_scope(){
                config.scope(Pooled.class, PooledProvider::new);
                config.component(NotSingleton.class, NotSingleton.class, new PooledLiteral());
                List<NotSingleton> instances = IntStream.range(0, 5).mapToObj(i -> config.getContext().get(ComponentRef.of(NotSingleton.class)).get()).toList();
                assertEquals(MAX, new HashSet<>(instances).size());
            }
            @Test
            public void should_throw_exception_if_multi_scoped_provided(){
                assertThrows(ContextConfigException.class, ()-> config.component(NotSingleton.class, NotSingleton.class, new PooledLiteral(), new SingletonLiteral()));
            }
            @Singleton @Pooled
            static class MultiScopedAnnotated {
            }
            @Test
            public void should_throw_exception_if_multi_scoped_annotated(){
                assertThrows(ContextConfigException.class, ()-> config.component(MultiScopedAnnotated.class, MultiScopedAnnotated.class));
            }
            @Test
            public void should_throw_exception_if_scoped_undefined(){
                assertThrows(ContextConfigException.class, ()-> config.component(NotSingleton.class, NotSingleton.class, new PooledLiteral()));
            }
            @Nested
            public class WithQualifier{
                static class NotSingleton{
                }
                @Test
                public void should_not_be_singleton_scope_by_default(){
                    config.component(WithScope.NotSingleton.class, WithScope.NotSingleton.class, new SkyWaterLiteral());
                    Assertions.assertNotSame(config.getContext().get(ComponentRef.of(WithScope.NotSingleton.class, new SkyWaterLiteral())).get(),
                            config.getContext().get(ComponentRef.of(WithScope.NotSingleton.class, new SkyWaterLiteral())).get());
                }
                @Test
                public void should_bind_component_as_singleton_scoped(){
                    config.component(NotSingleton.class, NotSingleton.class, new SingletonLiteral(), new SkyWaterLiteral());
                    Assertions.assertSame(config.getContext().get(ComponentRef.of(NotSingleton.class, new SkyWaterLiteral())).get(),
                            config.getContext().get(ComponentRef.of(NotSingleton.class,new SkyWaterLiteral())).get());
                }
                @Singleton
                static class SingletonAnnotated implements Dependency{
                }
                @Test
                public void should_retrieve_scope_annotation_from_component(){
                    config.component(Dependency.class, SingletonAnnotated.class, new SkyWaterLiteral());
                    assertSame(config.getContext().get(ComponentRef.of(Dependency.class, new SkyWaterLiteral())).get(),
                            config.getContext().get(ComponentRef.of(Dependency.class,new SkyWaterLiteral())).get());
                }
            }
        }
    }

    @Nested
    class DSL{

        interface Api{
        }
        static class Implementation implements Api{
        }
        @Test
        public void should_be_instance_as_its_declaration_type(){
            Implementation instance = new Implementation();
            config.from(new Config(){
                Implementation implementation = instance;
            });
            Implementation actual = config.getContext().get(ComponentRef.of(Implementation.class)).get();
            assertSame(instance, actual);
        }
        @Test
        public void should_bind_component_as_its_owner_type(){
            config.from(new Config(){
                Implementation implementation;
            });
            Implementation actual = config.getContext().get(ComponentRef.of(Implementation.class)).get();
            assertNotNull(actual);
        }
        @Test
        public void should_bind_instance_using_export_type(){
            Implementation instance = new Implementation();
            config.from(new Config(){
                @Export(Api.class)
                Implementation implementation = instance;
            });
            Api actual = config.getContext().get(ComponentRef.of(Api.class)).get();
            assertSame(actual, instance);
        }
        @Test
        public void should_bind_component_using_export_type(){
            config.from(new Config(){
                @Export(Api.class)
                Implementation implementation;
            });
            Api actual = config.getContext().get(ComponentRef.of(Api.class)).get();
            assertNotNull(actual);
        }
        @Test
        public void should_bind_instance_with_qualifier(){
            Implementation instance = new Implementation();
            config.from(new Config(){
                @SkyWater
                Api implementation = instance;
            });
            Api actual = config.getContext().get(ComponentRef.of(Api.class, new SkyWaterLiteral())).get();
            assertSame(actual, instance);
        }
        @Test
        public void should_bind_component_with_qualifier(){
            config.from(new Config(){
                @SkyWater
                Implementation implementation;
            });
            Implementation actual = config.getContext().get(ComponentRef.of(Implementation.class, new SkyWaterLiteral())).get();
            assertNotNull(actual);
        }
    }
}

package org.tdd.item;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Provider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.lang.reflect.ParameterizedType;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@Nested
public class InjectTest {
    private Dependency dependency = mock(Dependency.class);
    private Context context = mock(Context.class);
    private Provider<Dependency> providerDependency = mock(Provider.class);
    private  ParameterizedType dependencyProviderType;


    @BeforeEach
    public void setup() throws NoSuchFieldException {
        dependencyProviderType = (ParameterizedType) InjectTest.class.getDeclaredField("providerDependency").getGenericType();
        when(context.get(eq(ComponentRef.of(Dependency.class)))).thenReturn(Optional.of(dependency));
        when(context.get(eq(ComponentRef.of(dependencyProviderType)))).thenReturn(Optional.of(providerDependency));
    }

    @Nested
    public class ConstructorInjection {
        static abstract class AbstractComponent {
            @Inject
            public AbstractComponent() {
            }
        }
        static class DefaultConstructor{
        }
        interface InterfaceComponent {
            @Inject
            default void inject() {
            }
        }
        static class InjectConstructor{
            private Dependency dependency;

            @Inject
            public InjectConstructor(Dependency dependency) {
                this.dependency = dependency;
            }
        }

        @Nested
        class Injection{

            @Test
            public void should_call_default_constructor_if_no_inject_constructor() {
                DefaultConstructor instance = new InjectionProvider<>(DefaultConstructor.class).get(context);

                assertNotNull(instance);
            }

            @Test
            public void should_inject_dependency_via_inject_constructor() {
                InjectConstructor instance = new InjectionProvider<>(InjectConstructor.class).get(context);
                assertNotNull(instance);
            }
            @Test
            public void should_include_constructor_dependency_in_dependencies() {
                InjectionProvider<InjectConstructor> provider = new InjectionProvider<>(InjectConstructor.class);
                assertArrayEquals(new ComponentRef[]{ComponentRef.of(Dependency.class)}, provider.getDependencies().toArray(ComponentRef[]::new));
            }
            static class ProviderInjectConstructor{
                @Inject
                public ProviderInjectConstructor(Provider<Dependency> dependency) {
                    this.dependency = dependency;
                }
                Provider<Dependency> dependency;
            }
            @Test
            public void should_inject_provider_via_inject_constructor(){
                ProviderInjectConstructor instance = new InjectionProvider<>(ProviderInjectConstructor.class).get(context);
                assertSame(providerDependency, instance.dependency);
            }
            @Test
            public void should_include_provider_type_from_inject_constructor(){
                InjectionProvider<ProviderInjectConstructor> provider = new InjectionProvider<>(ProviderInjectConstructor.class);
                assertArrayEquals(new ComponentRef[]{ComponentRef.of(dependencyProviderType)}, provider.getDependencies().toArray(ComponentRef[]::new));
            }
        }
        @Nested
        class IllegalInjectConstructors{
            class ComponentWithMultiInjectConstructor implements TestComponent {

                @Inject
                public ComponentWithMultiInjectConstructor(String name, Double value) {
                }

                @Inject
                public ComponentWithMultiInjectConstructor(String name) {
                }
            }

            class ComponentWithNoInjectNorDefaultConstructor implements TestComponent {

                public ComponentWithNoInjectNorDefaultConstructor(String name) {
                }
            }
            @Test
            public void should_throw_exception_if_component__is_abstract() {
                assertThrows(ComponentError.class, () -> new InjectionProvider<>((ConstructorInjection.AbstractComponent.class)));
            }

            @Test
            public void should_throw_exception_if_component_is_interface() {
                assertThrows(ComponentError.class, () -> new InjectionProvider<>((ConstructorInjection.InterfaceComponent.class)));
            }

            @Test
            public void should_throw_exception_if_multi_inject_constructors_provided() {
                assertThrows(ComponentError.class, () -> new InjectionProvider<>(ComponentWithMultiInjectConstructor.class));
            }

            @Test
            public void should_throw_exception_if_no_inject_nor_default_constructors_provided() {
                assertThrows(ComponentError.class, () -> new InjectionProvider<>(ComponentWithNoInjectNorDefaultConstructor.class));
            }
        }
        @Nested
        class WithQualifier{
            @BeforeEach
            public void setup(){
                Mockito.reset(context);
                when(context.get(eq(ComponentRef.of(Dependency.class, new NameLiteral("ChosenOne"))))).thenReturn(Optional.of(dependency));
            }
            @Test
            public void should_inject_dependency_with_qualifier_via_constructor(){
                InjectionProvider<InjectConstructor> provider = new InjectionProvider<>(InjectConstructor.class);
                InjectConstructor component = provider.get(context);
                assertSame(component.dependency, dependency);
            }
            static class InjectConstructor{
                Dependency dependency;
                @Inject
                public InjectConstructor(@Named("ChosenOne") Dependency dependency) {
                    this.dependency = dependency;
                }
            }
            @Test
            public void should_include_qualifier_with_dependency(){
                InjectionProvider<InjectConstructor> provider = new InjectionProvider<>(InjectConstructor.class);
                assertArrayEquals(new ComponentRef<?>[]{ComponentRef.of(Dependency.class, new NameLiteral("ChosenOne"))},
                        provider.getDependencies().toArray());
            }
            static class MultiQualifierInjectConstructor{
                @Inject
                public MultiQualifierInjectConstructor(@Named("ChosenOne") @SkyWater Dependency dependency) {
                }
            }
            @Test
            public void should_throw_exception_if_multi_qualifiers_given(){
                assertThrows(ComponentError.class, ()-> new InjectionProvider<>(MultiQualifierInjectConstructor.class));
            }
        }
    }

    @Nested
    public class FieldInjection {
        static class ComponentWithFieldInjection {
            @Inject
            Dependency dependency;
        }
        static class SubClassWithFieldInjection extends FieldInjection.ComponentWithFieldInjection {
        }

        static class ComponentWithFinalFieldInjection {
            @Inject
            final Dependency dependency = new Dependency() {
            };
        }

        @Nested
        class Injection{
            @Test
            public void should_inject_dependency_via_field() {
                ComponentWithFieldInjection component = new InjectionProvider<>(ComponentWithFieldInjection.class).get(context);;
                assertSame(dependency, component.dependency);
            }

            @Test
            public void should_include_dependency_from_field_dependencies() {
                InjectionProvider<ComponentWithFieldInjection> provider = new InjectionProvider<>(FieldInjection.ComponentWithFieldInjection.class);
                assertArrayEquals(new ComponentRef[]{ComponentRef.of(Dependency.class)}, provider.getDependencies().toArray(ComponentRef[]::new));
            }

            @Test
            public void should_inject_dependency_via_subclass_inject_field() {
                SubClassWithFieldInjection component = new InjectionProvider<>(SubClassWithFieldInjection.class).get(context);
                assertSame(dependency, component.dependency);
            }
            static class ProviderInjectField{
                @Inject
                Provider<Dependency> dependency;
            }

            @Test
            public void should_inject_provider_via_inject_field(){
                ProviderInjectField instance = new InjectionProvider<>(ProviderInjectField.class).get(context);
                assertSame(providerDependency, instance.dependency);
            }
            @Test
            public void should_include_provider_type_from_inject_field() {
                InjectionProvider<ProviderInjectField> provider = new InjectionProvider<>(ProviderInjectField.class);
                assertArrayEquals(new ComponentRef[]{ComponentRef.of(dependencyProviderType)}, provider.getDependencies().toArray(ComponentRef[]::new));
            }
        }
        @Nested
        class IllegalInjectFields{
            @Test
            public void should_throw_exception_when_dependency_final_field() {
                assertThrows(ComponentError.class, () -> new InjectionProvider<>(ComponentWithFinalFieldInjection.class));
            }
        }
        @Nested
        class WithQualifier{
            @BeforeEach
            public void setup(){
                Mockito.reset(context);
                when(context.get(eq(ComponentRef.of(Dependency.class, new NameLiteral("ChosenOne"))))).thenReturn(Optional.of(dependency));
            }
            @Test
            public void should_inject_dependency_with_qualifier_via_field(){
                InjectionProvider<InjectField> provider = new InjectionProvider<>(InjectField.class);
                InjectField component = provider.get(context);
                assertSame(component.dependency, dependency);
            }
            static class InjectField {
                @Inject
                @Named("ChosenOne") Dependency dependency;
            }
            @Test
            public void should_include_qualifier_with_dependency(){
                InjectionProvider<InjectField> provider = new InjectionProvider<>(InjectField.class);
                assertArrayEquals(new ComponentRef<?>[]{ComponentRef.of(Dependency.class, new NameLiteral("ChosenOne"))},
                        provider.getDependencies().toArray());
            }
            static class MultiQualifierInjectField{
                @Inject
                @Named("ChosenOne") @SkyWater Dependency dependency;
            }
            @Test
            public void should_throw_exception_if_multi_qualifiers_given(){
                assertThrows(ComponentError.class, ()-> new InjectionProvider<>(MultiQualifierInjectField.class));
            }
        }
    }

    @Nested
    public class MethodInjection {
        static class InjectMethodWithNoDependency {
            boolean called = false;

            @Inject
            void install() {
                called = true;
            }
        }

        static class InjectMethodWithDependency {
            Dependency dependency;

            @Inject
            void install(Dependency dependency) {
                this.dependency = dependency;
            }

        }

        static class SupperClassWithInjectMethod {
            int superCalled = 0;

            @Inject
            void install() {
                superCalled++;
            }
        }

        static class SubClassWithInjectMethod extends MethodInjection.SupperClassWithInjectMethod {
            int subCalled = 0;
            @Inject
            void anotherInstall() {
                subCalled = superCalled + 1;
            }
        }
        static class SubClassOverrideSuperClassWithInject extends MethodInjection.SupperClassWithInjectMethod {
            @Inject
            void install() {
                super.install();
            }
        }
        static class SubClassOverrideSupClassWithNoInject extends MethodInjection.SupperClassWithInjectMethod {
            void install() {
                super.install();
            }
        }

        static class InjectMethodWithTypeParameter {
            @Inject
            <T> void install() {
            }
        }

        @Nested
        class Injection{
            @Test
            public void should_call_inject_method_even_if_no_dependency_declared() {
                InjectMethodWithNoDependency component = new InjectionProvider<>(InjectMethodWithNoDependency.class).get(context);
                assertTrue(component.called);
            }

            @Test
            public void should_inject_method_via_inject_method() {
                InjectMethodWithDependency component = new InjectionProvider<>(InjectMethodWithDependency.class).get(context);
                assertSame(dependency, component.dependency);

            }

            @Test
            public void should_include_dependencies_from_inject_method() {
                InjectionProvider<InjectMethodWithDependency> provider = new InjectionProvider<>(MethodInjection.InjectMethodWithDependency.class);
                assertArrayEquals(new ComponentRef[]{ComponentRef.of(Dependency.class)}, provider.getDependencies().toArray(ComponentRef[]::new));
            }

            @Test
            public void should_inject_dependencies_via_inject_method_from_superclass() {
                SubClassWithInjectMethod component = new InjectionProvider<>(SubClassWithInjectMethod.class).get(context);
                assertEquals(2, component.subCalled);
                assertEquals(1, component.superCalled);
            }

            @Test
            public void should_only_call_once_if_subclass_override_inject_method_with_inject() {
                SubClassOverrideSuperClassWithInject component = new InjectionProvider<>(SubClassOverrideSuperClassWithInject.class).get(context);
                assertEquals(1, component.superCalled);
            }

            @Test
            public void should_not_call_inject_method_if_override_with_no_inject() {
                SubClassOverrideSupClassWithNoInject component = new InjectionProvider<>(SubClassOverrideSupClassWithNoInject.class).get(context);
                assertEquals(0, component.superCalled);
            }
            static class ProviderInjectMethod{

                @Inject
                public void install(Provider<Dependency> dependency) {
                    this.dependency = dependency;
                }
                Provider<Dependency> dependency;
            }
            @Test
            public void should_inject_provider_via_inject_method(){
                ProviderInjectMethod instance = new InjectionProvider<>(ProviderInjectMethod.class).get(context);
                assertSame(providerDependency, instance.dependency);
            }
            @Test
            public void should_include_provider_type_from_inject_method() {
                InjectionProvider<ProviderInjectMethod> provider = new InjectionProvider<>(ProviderInjectMethod.class);
                assertArrayEquals(new ComponentRef[]{ComponentRef.of(dependencyProviderType)}, provider.getDependencies().toArray(ComponentRef[]::new));
            }
        }

        @Nested
        class IllegalInjectMethods{
            @Test
            public void should_throw_exception_inject_method_if_type_parameter_defined() {
                assertThrows(ComponentError.class, () -> new InjectionProvider<>(MethodInjection.InjectMethodWithTypeParameter.class));
            }
        }
        @Nested
        class WithQualifier{
            @BeforeEach
            public void setup(){
                Mockito.reset(context);
                when(context.get(eq(ComponentRef.of(Dependency.class, new NameLiteral("ChosenOne"))))).thenReturn(Optional.of(dependency));
            }
            @Test
            public void should_inject_dependency_with_qualifier_via_method(){
                InjectionProvider<InjectMethod> provider = new InjectionProvider<>(InjectMethod.class);
                InjectMethod component = provider.get(context);
                assertSame(component.dependency, dependency);
            }
            static class InjectMethod {
                Dependency dependency;
                @Inject
                void install(@Named("ChosenOne") Dependency dependency) {
                    this.dependency = dependency;
                }
            }
            @Test
            public void should_include_qualifier_with_dependency(){
                InjectionProvider<InjectMethod> provider = new InjectionProvider<>(InjectMethod.class);
                assertArrayEquals(new ComponentRef<?>[]{ComponentRef.of(Dependency.class, new NameLiteral("ChosenOne"))},
                        provider.getDependencies().toArray());
            }
            static class MultiQualifierInjectMethod{
                @Inject
                void install(@Named("ChosenOne") @SkyWater Dependency dependency) {
                }
            }
            @Test
            public void should_throw_exception_if_multi_qualifiers_given(){
                assertThrows(ComponentError.class, ()-> new InjectionProvider<>(MultiQualifierInjectMethod.class));
            }
        }
    }
}

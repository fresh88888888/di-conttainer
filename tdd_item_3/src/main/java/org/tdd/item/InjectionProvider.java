package org.tdd.item;

import jakarta.inject.Inject;

import java.lang.reflect.*;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.stream;
import static java.util.stream.Stream.concat;
import static org.tdd.item.ComponentError.*;

class InjectionProvider<T> implements ComponentProvider<T> {
    private final Injectable<Constructor<T>> injectConstructor;
    private final Map<Class<?>, List<Injectable<Method>>> injectMethods;
    private final Map<Class<?>, List<Injectable<Field>>> injectFields;
    private final Collection<Class<?>> superClasses;
    private final List<ComponentRef<?>> dependencies;

    public InjectionProvider(Class<T> component) {
        this.injectConstructor = getInjectConstructor(component);
        this.superClasses = allSuperClass(component);
        var injectFields = getInjectFields(component);
        var injectMethods = getInjectMethods(component);
        this.dependencies = getDependencies(injectFields, injectMethods);
        this.injectFields = groupByClass(injectFields);
        this.injectMethods = groupByClass(injectMethods);
    }

    @Override
    public T get(Context context) {
        try {
            T instance = injectConstructor.element().newInstance(injectConstructor.toDependencies(context));
            for (Class<?> c : superClasses) {
                for (Injectable<Field> field : injectFields.getOrDefault(c, List.of())) {
                    field.element().set(instance, field.toDependencies(context)[0]);
                }
                for (Injectable<Method> method : injectMethods.getOrDefault(c, List.of())) {
                    method.element().invoke(instance, method.toDependencies(context));
                }
            }

            return instance;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
    @Override
    public List<ComponentRef<?>> getDependencies() {
        return dependencies;
    }
    private  List<ComponentRef<?>> getDependencies(List<Injectable<Field>> injectFields, List<Injectable<Method>> injectMethods) {
        return concat(concat(Stream.of(injectConstructor), injectFields.stream()), injectMethods.stream()).map(Injectable::required).flatMap(Arrays::stream).toList();
    }
    private static <E extends AccessibleObject> Map<Class<?>, List<Injectable<E>>> groupByClass(List<Injectable<E>> injectFields) {
        return injectFields.stream().collect(Collectors.groupingBy(i -> ((Member) i.element()).getDeclaringClass(), Collectors.toList()));
    }

    private static <T> List<Injectable<Field>> getInjectFields(Class<T> component) {
        List<Injectable<Field>> injectableFields = InjectionProvider.<Field>traverse(component, (fields, current) -> injectable(current.getDeclaredFields()).toList()).stream().map(Injectable::of).toList();
        return check(component, injectableFields, InjectionProvider::notFinal, ComponentError::finalInjectFields);
    }

    private static <Type> Injectable<Constructor<Type>> getInjectConstructor(Class<Type> implementation) {
        if (Modifier.isAbstract(implementation.getModifiers())) {
            throw abstractComponent(implementation);
        }
        List<Constructor<?>> injectConstructors = injectable(implementation.getDeclaredConstructors()).toList();
        if (injectConstructors.size() > 1) {
            throw ambiguousInjectableConstructors(implementation);
        }

        return Injectable.of((Constructor<Type>) injectConstructors.stream().findFirst().orElseGet(() -> defaultConstructor(implementation)));
    }

    private static <T> List<Injectable<Method>> getInjectMethods(Class<T> component) {
        List<Method> injectMethods = traverse(component, (method, current) -> injectable(current.getDeclaredMethods())
                .filter(m -> isOverrideByInjectMethod(method, m))
                .filter(m -> isOverrideByNoInjectMethod(component, m)).toList());
        List<Injectable<Method>> injectableMethods = injectMethods.stream().map(Injectable::of).toList();

        return check(component, injectableMethods, InjectionProvider::noTypeParameter, ComponentError::injectMethodsWithTypeParameter);
    }

    private static <Element extends AccessibleObject> List<Injectable<Element>> check(Class<?> component, List<Injectable<Element>> target, Predicate<Element> predicate,
                                                                                      BiFunction<Class<?>, List<Element>, ComponentError> error) {
        List<Element> found = target.stream().map(Injectable::element).filter(predicate).toList();
        if (found.size() > 0) {
            throw error.apply(component, found.stream().toList());
        }
        return target;
    }

    private static Collection<Class<?>> allSuperClass(Class<?> component) {
        List<Class<?>> result = new ArrayList<>();
        for (Class superClass = component; superClass != Object.class; superClass = superClass.getSuperclass()) {
            result.add(0, superClass);
        }

        return result;
    }

    private static <T> List<T> traverse(Class<?> component, BiFunction<List<T>, Class<?>, List<T>> finder) {
        List<T> members = new ArrayList<>();
        Class<?> current = component;
        while (current != Object.class) {
            members.addAll(finder.apply(members, current));
            current = current.getSuperclass();
        }

        return members;
    }

    private static <Type> Constructor<Type> defaultConstructor(Class<Type> implementation) {
        try {
            return implementation.getDeclaredConstructor();
        } catch (NoSuchMethodException e) {
            throw noDefaultConstructor(implementation);
        }
    }

    private static <T extends AnnotatedElement> Stream<T> injectable(T[] declaredFields) {
        return stream(declaredFields).filter(f -> f.isAnnotationPresent(Inject.class));
    }

    private static boolean notFinal(Field field) {
        return Modifier.isFinal(field.getModifiers());
    }

    private static boolean noTypeParameter(Method method) {
        return method.getTypeParameters().length != 0;
    }

    private static boolean isOverrideByInjectMethod(List<Method> injectMethods, Method m) {
        return injectMethods.stream().noneMatch(o -> isOverride(m, o));
    }

    private static <T> boolean isOverrideByNoInjectMethod(Class<T> component, Method m) {
        return stream(component.getDeclaredMethods()).filter(m1 -> !m1.isAnnotationPresent(Inject.class)).noneMatch(o -> isOverride(m, o));
    }

    private static boolean isOverride(Method m, Method o) {
        boolean visible;
        if (m.getDeclaringClass().getPackageName().equals(o.getDeclaringClass().getPackageName())) {
            visible = !Modifier.isPrivate(o.getModifiers()) && !Modifier.isPrivate(m.getModifiers());
        }
        else {
            visible = (Modifier.isPublic(o.getModifiers()) || Modifier.isProtected(o.getModifiers())) && (Modifier.isPublic(m.getModifiers()) || Modifier.isProtected(m.getModifiers()));
        }

        return visible && o.getName().equals(m.getName()) && Arrays.equals(o.getParameterTypes(), m.getParameterTypes());
    }
}

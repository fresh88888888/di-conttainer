package org.ws.tdd.rest;

import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.container.ResourceContext;
import jakarta.ws.rs.core.GenericEntity;
import jakarta.ws.rs.core.Response;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

public class ResourceMethods {
    private Map<String, List<ResourceRouter.ResourceMethod>> resourceMethods;

    public ResourceMethods(Method[] methods) {
        this.resourceMethods = getResourceMethods(methods);
    }

    private static Map<String, List<ResourceRouter.ResourceMethod>> getResourceMethods(Method[] methods) {
        return Arrays.stream(methods)
                .filter(m -> Arrays.stream(m.getAnnotations()).anyMatch(a -> a.annotationType().isAnnotationPresent(HttpMethod.class)))
                .map(DefaultResourceMethod::new)
                .collect(Collectors.groupingBy(ResourceRouter.ResourceMethod::getHttpMethod));
    }

    public Optional<ResourceRouter.ResourceMethod> findResourceMethods(String path, String method) {
        return findMethod(path, method).or(() -> findAlterNative(path, method));
    }

    private Optional<ResourceRouter.ResourceMethod> findAlterNative(String path, String method) {
        if (HttpMethod.HEAD.equals(method)) {
            return findMethod(path, HttpMethod.GET).map(HeadResourceMethod::new);
        }
        if (HttpMethod.OPTIONS.equals(method)) {
            return Optional.of(new OptionsResourceMethod(path));
        }
        return Optional.empty();
    }

    private Optional<ResourceRouter.ResourceMethod> findMethod(String path, String method) {
        return Optional.ofNullable(resourceMethods.get(method)).flatMap(methods -> UriHandlers.match(path, methods, r -> r.getRemaining() == null));
    }

    class OptionsResourceMethod implements ResourceRouter.ResourceMethod {
        private String path;
        private static final List<String> methods = List.of(HttpMethod.GET, HttpMethod.OPTIONS, HttpMethod.HEAD, HttpMethod.DELETE, HttpMethod.PUT, HttpMethod.PATCH, HttpMethod.POST);

        public OptionsResourceMethod(String path) {
            this.path = path;
        }

        @Override
        public String getHttpMethod() {
            return HttpMethod.OPTIONS;
        }

        @Override
        public GenericEntity<?> call(ResourceContext context, UriInfoBuilder builder) {
            return new GenericEntity<>(Response.noContent().allow(findAllowedMethods()).build(), Response.class);
        }

        private Set<String> findAllowedMethods() {
            Set<String> allowed = methods.stream().filter(method -> findMethod(path, method).isPresent()).collect(Collectors.toSet());
            allowed.add(HttpMethod.OPTIONS);
            if (allowed.contains(HttpMethod.GET)) {
                allowed.add(HttpMethod.HEAD);
            }
            return allowed;
        }

        @Override
        public UriTemplate getUriTemplate() {
            return new PathTemplate(path);
        }
    }
}

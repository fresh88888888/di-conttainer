package org.ws.tdd.rest;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.container.ResourceContext;
import jakarta.ws.rs.core.GenericEntity;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;

import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

interface ResourceRouter {
    OutboundResponse dispatch(HttpServletRequest req, ResourceContext resourceContext);

    interface Resource extends UriHandler {
        Optional<ResourceMethod> match(UriTemplate.MatchResult result, String httpMethod, String[] mediaTypes, ResourceContext context, UriInfoBuilder builder);
    }

    interface ResourceMethod extends UriHandler {
        String getHttpMethod();

        GenericEntity<?> call(ResourceContext context, UriInfoBuilder builder);
    }
}

class DefaultResourceRouter implements ResourceRouter {
    private Runtime runtime;
    private List<Resource> rootResources;

    public DefaultResourceRouter(Runtime runtime, List<Resource> rootResources) {
        this.runtime = runtime;
        this.rootResources = rootResources;
    }

    @Override
    public OutboundResponse dispatch(HttpServletRequest req, ResourceContext resourceContext) {
        String path = req.getServletPath();
        UriInfoBuilder builder = runtime.createUriInfoBuilder(req);
        Optional<ResourceMethod> method = UriHandlers.mapMatched(path, rootResources, (result, resource) -> findResourceMethod(req, resourceContext, builder, result, resource));

        if (method.isEmpty()) {
            return (OutboundResponse) Response.status(Response.Status.NOT_FOUND).build();
        }

        return (OutboundResponse) method.map(m -> m.call(resourceContext, builder))
                .map(entity -> (entity.getEntity() instanceof OutboundResponse) ? (OutboundResponse) entity.getEntity() : Response.ok(entity).build())
                .orElseGet(() -> Response.noContent().build());
    }

    private Optional<ResourceMethod> findResourceMethod(HttpServletRequest req, ResourceContext resourceContext, UriInfoBuilder builder, Optional<UriTemplate.MatchResult> matched, Resource handler) {
        return handler.match(matched.get(), req.getMethod(), Collections.list(req.getHeaders(HttpHeaders.ACCEPT)).toArray(String[]::new), resourceContext, builder);
    }
}

class ResourceMethods {
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
            List<String> methods = List.of(HttpMethod.GET, HttpMethod.OPTIONS, HttpMethod.HEAD, HttpMethod.DELETE, HttpMethod.PUT, HttpMethod.PATCH, HttpMethod.POST);
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

class DefaultResourceMethod implements ResourceRouter.ResourceMethod {
    private String httpMethod;
    private UriTemplate uriTemplate;
    private Method method;

    public DefaultResourceMethod(Method method) {
        this.method = method;
        this.uriTemplate = new PathTemplate(Optional.ofNullable(method.getAnnotation(Path.class)).map(Path::value).orElse(""));
        this.httpMethod = Arrays.stream(method.getAnnotations()).filter(a -> a.annotationType().isAnnotationPresent(HttpMethod.class))
                .findFirst().get().annotationType().getAnnotation(HttpMethod.class).value();
    }

    @Override
    public UriTemplate getUriTemplate() {
        return uriTemplate;
    }

    @Override
    public String getHttpMethod() {
        return httpMethod;
    }

    @Override
    public GenericEntity<?> call(ResourceContext context, UriInfoBuilder builder) {
        Object result = MethodInvoker.invoke(method, context, builder);
        return result != null ? new GenericEntity<>(result, method.getGenericReturnType()) : null;
    }

    @Override
    public String toString() {
        return method.getDeclaringClass().getSimpleName() + "." + method.getName();
    }
}

class HeadResourceMethod implements ResourceRouter.ResourceMethod {
    private ResourceRouter.ResourceMethod method;

    public HeadResourceMethod(ResourceRouter.ResourceMethod method) {
        this.method = method;
    }

    @Override
    public String getHttpMethod() {
        return HttpMethod.HEAD;
    }

    @Override
    public GenericEntity<?> call(ResourceContext context, UriInfoBuilder builder) {
        return method.call(context, builder);
    }

    @Override
    public UriTemplate getUriTemplate() {
        return method.getUriTemplate();
    }
}

class ResourceHandler implements ResourceRouter.Resource {
    private UriTemplate uriTemplate;
    private ResourceMethods resourceMethods;
    private SubResourceLocators subResourceLocators;
    private Function<ResourceContext, Object> resource;

    public ResourceHandler(Class<?> resourceClass) {
        this(resourceClass, new PathTemplate(getTemplate(resourceClass)), rc -> rc.getResource(resourceClass));
    }

    public ResourceHandler(Object resource, UriTemplate uriTemplate) {
        this(resource.getClass(), uriTemplate, re -> resource);
    }

    private ResourceHandler(Class<?> resourceClass, UriTemplate uriTemplate, Function<ResourceContext, Object> resource) {
        this.uriTemplate = uriTemplate;
        this.resourceMethods = new ResourceMethods(resourceClass.getMethods());
        this.subResourceLocators = new SubResourceLocators(resourceClass.getMethods());
        this.resource = resource;
    }

    private static String getTemplate(Class<?> resourceClass) {
        if (!resourceClass.isAnnotationPresent(Path.class)) {
            throw new IllegalArgumentException();
        }
        return resourceClass.getAnnotation(Path.class).value();
    }

    @Override
    public Optional<ResourceRouter.ResourceMethod> match(UriTemplate.MatchResult result, String httpMethod, String[] mediaTypes, ResourceContext context, UriInfoBuilder builder) {
        String remaining = Optional.ofNullable(result.getRemaining()).orElse("");
        builder.addMatchedResource(resource.apply(context));
        builder.addMatchedPathParameters(result.getMatchedPathParameters());
        return resourceMethods.findResourceMethods(remaining, httpMethod).or(() -> subResourceLocators.findSubResourceMethods(remaining, httpMethod, mediaTypes, context, builder));
    }

    @Override
    public UriTemplate getUriTemplate() {
        return uriTemplate;
    }
}

class SubResourceLocators {
    private List<ResourceRouter.Resource> subResourceLocators;

    public SubResourceLocators(Method[] methods) {
        subResourceLocators = Arrays.stream(methods)
                .filter(m -> m.isAnnotationPresent(Path.class) && Arrays.stream(m.getAnnotations()).noneMatch(a -> a.annotationType().isAnnotationPresent(HttpMethod.class)))
                .map((Function<Method, ResourceRouter.Resource>) SubResourceLocator::new).toList();
    }

    public Optional<ResourceRouter.ResourceMethod> findSubResourceMethods(String path, String method, String[] mediaTypes, ResourceContext context, UriInfoBuilder builder) {
        return UriHandlers.mapMatched(path, subResourceLocators, (result, locator) -> locator.match(result.get(), method, mediaTypes, context, builder));
    }

    static class SubResourceLocator implements ResourceRouter.Resource {
        private PathTemplate uriTemplate;
        private Method method;

        public SubResourceLocator(Method method) {
            this.method = method;
            this.uriTemplate = new PathTemplate(method.getAnnotation(Path.class).value());
        }

        @Override
        public UriTemplate getUriTemplate() {
            return uriTemplate;
        }

        @Override
        public Optional<ResourceRouter.ResourceMethod> match(UriTemplate.MatchResult result, String httpMethod, String[] mediaTypes, ResourceContext context, UriInfoBuilder builder) {
            try {
                builder.addMatchedPathParameters(result.getMatchedPathParameters());
                Object subResource = MethodInvoker.invoke(method, context, builder);
                return new ResourceHandler(subResource, uriTemplate).match(excludePathParameters(result), httpMethod, mediaTypes, context, builder);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        private UriTemplate.MatchResult excludePathParameters(UriTemplate.MatchResult result) {
            return new UriTemplate.MatchResult() {
                @Override
                public String getMatched() {
                    return result.getMatched();
                }
                @Override
                public String getRemaining() {
                    return result.getRemaining();
                }
                @Override
                public Map<String, String> getMatchedPathParameters() {
                    return new HashMap<>();
                }
                @Override
                public int compareTo(UriTemplate.MatchResult o) {
                    return result.compareTo(o);
                }
            };
        }

        @Override
        public String toString() {
            return method.getDeclaringClass().getSimpleName() + "." + method.getName();
        }
    }
}
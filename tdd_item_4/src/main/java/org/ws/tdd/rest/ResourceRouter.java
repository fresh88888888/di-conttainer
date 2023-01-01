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

    interface Resource {
        Optional<ResourceMethod> match(UriTemplate.MatchResult result, String method, String[] mediaTypes, ResourceContext context, UriInfoBuilder builder);
    }

    interface RootResource extends Resource, UriHandler {
    }

    interface ResourceMethod extends UriHandler{
        String getHttpMethod();
        GenericEntity<?> call(ResourceContext context, UriInfoBuilder builder);
    }
    interface SubResourceLocator extends UriHandler{
        Resource getSubResource(ResourceContext context, UriInfoBuilder uriInfoBuilder);
    }
}
class DefaultResourceRouter implements ResourceRouter {
    private Runtime runtime;
    private List<RootResource> rootResources;

    public DefaultResourceRouter(Runtime runtime, List<RootResource> rootResources) {
        this.runtime = runtime;
        this.rootResources = rootResources;
    }
    @Override
    public OutboundResponse dispatch(HttpServletRequest req, ResourceContext resourceContext) {
        String path = req.getServletPath();
        UriInfoBuilder builder = runtime.createUriInfoBuilder(req);
        Optional<ResourceMethod> method = UriHandlers.mapMatched(path, rootResources, (result, resource) -> findResourceMethod(req, resourceContext, builder, result, resource));

        if (method.isEmpty()){
            return (OutboundResponse) Response.status(Response.Status.NOT_FOUND).build();
        }

        return (OutboundResponse) method.map(m -> m.call(resourceContext, builder)).map(entity -> Response.ok(entity).build()).orElseGet(() -> Response.noContent().build());
    }
    private Optional<ResourceMethod> findResourceMethod(HttpServletRequest req, ResourceContext resourceContext, UriInfoBuilder builder, Optional<UriTemplate.MatchResult> matched, RootResource handler) {
        return handler.match(matched.get(), req.getMethod(), Collections.list(req.getHeaders(HttpHeaders.ACCEPT)).toArray(String[]::new), resourceContext, builder);
    }
}
class ResourceMethods{
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
            return Optional.ofNullable(resourceMethods.get(method)).flatMap(methods -> UriHandlers.match(path, methods, r -> r.getRemaining() == null));
        }
    }

class DefaultResourceMethod implements ResourceRouter.ResourceMethod{
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
            return null;
        }
        @Override
        public String toString() {
            return method.getDeclaringClass().getSimpleName() + "." + method.getName();
        }
}
class RootResourceClass implements ResourceRouter.RootResource{
    private PathTemplate uriTemplate;
    private Class<?> resourceClass;
    private ResourceMethods resourceMethods;
    private SubResourceLocators subResourceLocators;
    public RootResourceClass(Class<?> resourceClass) {
        this.resourceClass = resourceClass;
        this.uriTemplate  = new PathTemplate(resourceClass.getAnnotation(Path.class).value());
        this.resourceMethods = new ResourceMethods(resourceClass.getMethods());
        this.subResourceLocators = new SubResourceLocators(resourceClass.getMethods());
    }

    @Override
    public Optional<ResourceRouter.ResourceMethod> match(UriTemplate.MatchResult result, String method, String[] mediaTypes, ResourceContext context, UriInfoBuilder builder) {
        String remaining = Optional.ofNullable(result.getRemaining()).orElse("");
        builder.addMatchedResource(context.getResource(resourceClass));
        return resourceMethods.findResourceMethods(remaining, method).or(() -> subResourceLocators.findSubResourceMethods(remaining, method, mediaTypes, context, builder));
    }

    @Override
    public UriTemplate getUriTemplate() {
        return uriTemplate;
    }
}
class SubResource implements ResourceRouter.Resource{
    private Object subResource;
    private ResourceMethods resourceMethods;
    private SubResourceLocators subResourceLocators;
    public SubResource(Object subResource) {
        this.subResource = subResource;
        this.resourceMethods = new ResourceMethods(subResource.getClass().getMethods());
        this.subResourceLocators = new SubResourceLocators(subResource.getClass().getMethods());
    }
    @Override
    public Optional<ResourceRouter.ResourceMethod> match(UriTemplate.MatchResult result, String method, String[] mediaTypes, ResourceContext context, UriInfoBuilder builder) {
        String remaining = Optional.ofNullable(result.getRemaining()).orElse("");
        return resourceMethods.findResourceMethods(remaining, method).or(() -> subResourceLocators.findSubResourceMethods(remaining, method, mediaTypes, context, builder));
    }
}
class SubResourceLocators{
    private List<ResourceRouter.SubResourceLocator> subResourceLocators;
    public SubResourceLocators(Method[] methods) {
        subResourceLocators = Arrays.stream(methods)
                .filter(m -> m.isAnnotationPresent(Path.class) && Arrays.stream(m.getAnnotations()).noneMatch(a -> a.annotationType().isAnnotationPresent(HttpMethod.class)))
                .map((Function<Method, ResourceRouter.SubResourceLocator>)DefaultSubResourceLocator::new).toList();
    }
    public Optional<ResourceRouter.SubResourceLocator> finSubResource(String path) {
        return UriHandlers.match(path, subResourceLocators);
    }
    public Optional<ResourceRouter.ResourceMethod> findSubResourceMethods(String path, String method, String[] mediaTypes, ResourceContext context, UriInfoBuilder builder){
       return UriHandlers.mapMatched(path, subResourceLocators, (result, locator) -> locator.getSubResource(context, builder).match(result.get(), method, mediaTypes, context, builder));
    }
    static class DefaultSubResourceLocator implements ResourceRouter.SubResourceLocator{
        private PathTemplate uriTemplate;
        private Method method;
        public DefaultSubResourceLocator(Method method) {
            this.method = method;
            this.uriTemplate = new PathTemplate(method.getAnnotation(Path.class).value());
        }
        @Override
        public UriTemplate getUriTemplate() {
            return uriTemplate;
        }
        @Override
        public String toString() {
            return method.getDeclaringClass().getSimpleName() + "." + method.getName();
        }
        @Override
        public ResourceRouter.Resource getSubResource(ResourceContext context, UriInfoBuilder uriInfoBuilder) {
            Object resource = uriInfoBuilder.getLastMatchedResource();
            try {
                Object subResource = method.invoke(resource);
                uriInfoBuilder.addMatchedResource(subResource);
                return new SubResource(subResource);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
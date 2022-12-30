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
import java.util.stream.Collectors;

interface ResourceRouter {
    OutboundResponse dispatch(HttpServletRequest req, ResourceContext resourceContext);

    interface Resource {
        Optional<ResourceMethod> match(UriTemplate.MatchResult result, String method, String[] mediaTypes, UriInfoBuilder builder);
    }

    interface RootResource extends Resource {
        UriTemplate getUriTemplate();
    }

    interface ResourceMethod {
        UriTemplate getUriTemplate();

        String getHttpMethod();

        GenericEntity<?> call(ResourceContext context, UriInfoBuilder builder);
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
        Optional<ResourceMethod> method = rootResources.stream().map(resource -> match(path, resource))
                .filter(Result::isMatched).sorted().findFirst().flatMap(result -> result.findResourceMethod(req, builder));

        if (method.isEmpty()){
            return (OutboundResponse) Response.status(Response.Status.NOT_FOUND).build();
        }

        return (OutboundResponse) method.map(m -> m.call(resourceContext, builder)).map(entity -> Response.ok(entity).build())
               .orElseGet(() -> Response.noContent().build());
    }

    private Result match(String path, RootResource resource) {
        return new Result(resource.getUriTemplate().match(path), resource);
    }

    record Result(Optional<UriTemplate.MatchResult> matched, ResourceRouter.RootResource resource) implements Comparable<Result> {
        private  boolean isMatched() {
            return matched.isPresent();
        }
        @Override
        public int compareTo(Result o) {
            return matched.flatMap(x -> o.matched.map(x::compareTo)).orElse(0);
        }
        private Optional<ResourceMethod> findResourceMethod(HttpServletRequest req, UriInfoBuilder builder) {
            return matched.flatMap(result -> resource.match(result, req.getMethod(),
                    Collections.list(req.getHeaders(HttpHeaders.ACCEPT)).toArray(String[]::new), builder));
        }
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

        static private Result match(String path, ResourceRouter.ResourceMethod method){
           return new Result(method.getUriTemplate().match(path), method);
        }
        public Optional<ResourceRouter.ResourceMethod> findResourceMethods(String path, String method) {
            return Optional.ofNullable(resourceMethods.get(method))
                    .flatMap(methods -> methods.stream().map(m -> ResourceMethods.match(path, m)).filter(ResourceMethods.Result::isMatched).sorted()
                            .findFirst().map(ResourceMethods.Result::resourceMethod));
        }
        static record Result(Optional<UriTemplate.MatchResult> matched, ResourceRouter.ResourceMethod resourceMethod) implements Comparable<Result>{
            @Override
            public int compareTo(Result o) {
                return matched.flatMap(x -> o.matched.map(x::compareTo)).orElse(0);
            }
            public boolean isMatched(){
                return matched.map(r -> r.getRemaining() == null).orElse(false);
            }
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
    public RootResourceClass(Class<?> resourceClass) {
        this.resourceClass = resourceClass;
        this.uriTemplate  = new PathTemplate(resourceClass.getAnnotation(Path.class).value());
        this.resourceMethods = new ResourceMethods(resourceClass.getMethods());
    }

    @Override
    public Optional<ResourceRouter.ResourceMethod> match(UriTemplate.MatchResult result, String method, String[] mediaTypes, UriInfoBuilder builder) {
        String remaining = Optional.ofNullable(result.getRemaining()).orElse("");
        return resourceMethods.findResourceMethods(remaining, method);
    }

    @Override
    public UriTemplate getUriTemplate() {
        return uriTemplate;
    }
}
class SubResource implements ResourceRouter.Resource{
    private Object subResource;
    private ResourceMethods resourceMethods;
    public SubResource(Object subResource) {
        this.subResource = subResource;
        this.resourceMethods = new ResourceMethods(subResource.getClass().getMethods());
    }
    @Override
    public Optional<ResourceRouter.ResourceMethod> match(UriTemplate.MatchResult result, String method, String[] mediaTypes, UriInfoBuilder builder) {
        String remaining = Optional.ofNullable(result.getRemaining()).orElse("");
        return resourceMethods.findResourceMethods(remaining, method);
    }
}
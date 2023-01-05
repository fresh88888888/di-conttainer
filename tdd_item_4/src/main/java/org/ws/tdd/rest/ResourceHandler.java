package org.ws.tdd.rest;

import jakarta.ws.rs.Path;
import jakarta.ws.rs.container.ResourceContext;

import java.util.Optional;
import java.util.function.Function;

public class ResourceHandler implements ResourceRouter.Resource {
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

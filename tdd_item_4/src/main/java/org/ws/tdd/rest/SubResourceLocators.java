package org.ws.tdd.rest;

import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.container.ResourceContext;

import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;

public class SubResourceLocators {
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
            } catch (WebApplicationException e) {
                throw e;
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

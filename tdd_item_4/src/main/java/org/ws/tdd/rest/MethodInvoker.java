package org.ws.tdd.rest;

import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.container.ResourceContext;
import jakarta.ws.rs.core.UriInfo;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static org.ws.tdd.rest.MethodInvoker.ValueConverter.singleValued;

public class MethodInvoker {
    private static ValueProvider pathParam = (parameter, uriInfo) -> Optional.ofNullable(parameter.getAnnotation(PathParam.class)).map(annotation -> uriInfo.getPathParameters().get(annotation.value()));
    private static ValueProvider queryParam = (parameter, uriInfo) -> Optional.ofNullable(parameter.getAnnotation(QueryParam.class)).map(annotation -> uriInfo.getQueryParameters().get(annotation.value()));
    private static List<ValueProvider> providers = List.of(pathParam, queryParam);

    static Object invoke(Method method, ResourceContext context, UriInfoBuilder builder) {
        try {
            UriInfo uriInfo = builder.createUriInfo();
            return method.invoke(builder.getLastMatchedResource(), Arrays.stream(method.getParameters())
                    .map(parameter -> injectParameter(parameter, uriInfo).or(() -> injectContext(parameter, context, uriInfo)).orElse(null)).toArray(Object[]::new));
        } catch (InvocationTargetException e){
           if(e.getCause() instanceof WebApplicationException){
                throw (WebApplicationException) e.getCause();
           }
           throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static Optional<Object> injectContext(Parameter parameter, ResourceContext context, UriInfo uriInfo) {
        if (parameter.getType().equals(ResourceContext.class)) {
            return Optional.of(context);
        }
        if (parameter.getType().equals(UriInfo.class)) {
            return Optional.of(uriInfo);
        }
        return Optional.of(context.getResource(parameter.getType()));
    }

    private static Optional<Object> injectParameter(Parameter parameter, UriInfo uriInfo) {
        return providers.stream()
                .map(provider -> provider.provider(parameter, uriInfo))
                .filter(Optional::isPresent)
                .findFirst()
                .flatMap(values -> values.flatMap(v -> convert(parameter, v)));
    }

    private static Optional<Object> convert(Parameter parameter, List<String> values) {
        return PrimitiveConverter.converter(parameter, values)
                .or(() -> ConverterConstructor.convert(parameter.getType(), values.get(0)))
                .or(() -> ConverterFactory.convert(parameter.getType(), values.get(0)));
    }

    interface ValueProvider {
        Optional<List<String>> provider(Parameter parameter, UriInfo uriInfo);
    }

    interface ValueConverter<T> {
        T fromString(List<String> values);
        static <T> ValueConverter<T> singleValued(Function<String, T> converter) {
            return values -> converter.apply(values.get(0));
        }
    }
}

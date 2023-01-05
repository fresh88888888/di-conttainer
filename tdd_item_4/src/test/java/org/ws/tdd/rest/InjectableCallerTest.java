package org.ws.tdd.rest;

import jakarta.ws.rs.container.ResourceContext;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.RuntimeDelegate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public abstract class InjectableCallerTest {
    protected ResourceContext context;
    protected UriInfoBuilder builder;
    protected UriInfo uriInfo;
    protected MultivaluedHashMap<String, String> parameters;
    protected LastCal lastCal;
    protected DefaultResourceMethodTest.SomeServiceInContext service;
    protected RuntimeDelegate delegate;
    private Object resource;

    @BeforeEach
    public void setup() {
        lastCal = null;
        resource = initResource();

        context = mock(ResourceContext.class);
        builder = mock(UriInfoBuilder.class);
        uriInfo = mock(UriInfo.class);
        service = mock(DefaultResourceMethodTest.SomeServiceInContext.class);
        parameters = new MultivaluedHashMap<>();

        when(builder.getLastMatchedResource()).thenReturn(resource);
        when(builder.createUriInfo()).thenReturn(uriInfo);
        when(uriInfo.getPathParameters()).thenReturn(parameters);
        when(uriInfo.getQueryParameters()).thenReturn(parameters);
        when(context.getResource(eq(DefaultResourceMethodTest.SomeServiceInContext.class))).thenReturn(service);
        delegate = mock(RuntimeDelegate.class);
        RuntimeDelegate.setInstance(delegate);

        when(delegate.createResponseBuilder()).thenReturn(new StubResponseBuilder());
    }

    protected static String getMethodName(String name, List<? extends Class<?>> classStream) {
        return name + "(" + classStream.stream().map(Class::getSimpleName).collect(Collectors.joining(",")) + ")";
    }

    protected abstract Object initResource();

    @TestFactory
    public List<DynamicTest> inject_convertable_types() {
        List<DynamicTest> tests = new ArrayList<>();
        List<InjectableTypeTestCase> typeCases = List.of(
                new InjectableTypeTestCase(String.class, "string", "string"),
                new InjectableTypeTestCase(int.class, "1", 1),
                new InjectableTypeTestCase(double.class, "3.25", 3.25),
                new InjectableTypeTestCase(short.class, "128", (short) 128),
                new InjectableTypeTestCase(byte.class, "42", (byte) 42),
                new InjectableTypeTestCase(boolean.class, "true", true),
                new InjectableTypeTestCase(BigDecimal.class, "123456", new BigDecimal("123456")),
                new InjectableTypeTestCase(DefaultResourceMethodTest.Convert.class, "Factory", InjectableCallerTest.Convert.Factory)
        );
        List<String> paramTypes = List.of("getPathParam", "getQueryParam");

        for (String type : paramTypes) {
            for (InjectableTypeTestCase testCase : typeCases) {
                tests.add(DynamicTest.dynamicTest("should inject " + testCase.type().getSimpleName() + " to " + type, () -> {
                    verifyResourceMethodCalled(type, testCase.type(), testCase.name(), testCase.value());
                }));
            }
        }

        return tests;
    }

    @TestFactory
    public List<DynamicTest> inject_context_object() {
        List<DynamicTest> tests = new ArrayList<>();
        List<InjectableTypeTestCase> typeCases = List.of(
                new InjectableTypeTestCase(DefaultResourceMethodTest.SomeServiceInContext.class, "N/A", service),
                new InjectableTypeTestCase(ResourceContext.class, "N/A", context),
                new InjectableTypeTestCase(UriInfo.class, "N/A", uriInfo)
        );
        for (InjectableTypeTestCase testCase : typeCases) {
            tests.add(DynamicTest.dynamicTest("should inject " + testCase.type().getSimpleName() + " to Context", () -> {
                verifyResourceMethodCalled("getContext", testCase.type(), testCase.name(), testCase.value());
            }));
        }
        return tests;
    }

    //TODO: using default convertors for matrix, query, form, header, cookie
    //TODO: default convertors for List, Set, SortSet, Array
    private void verifyResourceMethodCalled(String method, Class<?> type, String paramString, Object paramValue) throws NoSuchMethodException {
        parameters.put("param", List.of(paramString));

        callInjectable(method, type);

        assertEquals(InjectableCallerTest.getMethodName(method, List.of(type)), lastCal.name());
        assertEquals(List.of(paramValue), lastCal.arguments());
    }

    protected abstract void callInjectable(String method, Class<?> type) throws NoSuchMethodException;

    enum Convert {
        Primitive, Constructor, Factory
    }

    static interface SomeServiceInContext {

    }
    record InjectableTypeTestCase(Class<?> type, String name, Object value){}
    record LastCal(String name, List<Object> arguments) {}
}

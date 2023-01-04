package org.ws.tdd.rest;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.container.ResourceContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.GenericEntity;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.UriInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.lang.reflect.Parameter;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DefaultResourceMethodTest {
    private ResourceContext context;
    private UriInfoBuilder builder;
    private CallableResourceMethods resource;
    private UriInfo uriInfo;
    private MultivaluedHashMap<String, String> parameters;
    private LastCal lastCal;
    private SomeServiceInContext service;

    @BeforeEach
    public void setup() {
        lastCal = null;
        resource = (CallableResourceMethods) Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[]{CallableResourceMethods.class}, (proxy, method, args) -> {
            lastCal = new LastCal(getMethodName(method.getName(), Arrays.stream(method.getParameters()).map(Parameter::getType).toList()), args != null ? List.of(args) : List.of());
            return method.getName().equals("getList") ? new ArrayList<>() : null;
        });

        context = mock(ResourceContext.class);
        builder = mock(UriInfoBuilder.class);
        uriInfo = mock(UriInfo.class);
        service = mock(SomeServiceInContext.class);
        parameters = new MultivaluedHashMap<>();

        when(builder.getLastMatchedResource()).thenReturn(resource);
        when(builder.createUriInfo()).thenReturn(uriInfo);
        when(uriInfo.getPathParameters()).thenReturn(parameters);
        when(uriInfo.getQueryParameters()).thenReturn(parameters);
        when(context.getResource(eq(SomeServiceInContext.class))).thenReturn(service);
    }

    private static String getMethodName(String name, List<? extends Class<?>> classStream) {
        return name + "(" + classStream.stream().map(Class::getSimpleName).collect(Collectors.joining(",")) + ")";
    }

    @Test
    public void should_call_resource_method() throws NoSuchMethodException {
        DefaultResourceMethod resourceMethod = getResourceMethod("get");
        resourceMethod.call(context, builder);
        assertEquals("get()", lastCal.name());
    }

    @Test
    public void should_use_resource_method_generic_return_type() throws NoSuchMethodException {
        DefaultResourceMethod resourceMethod = getResourceMethod("getList");

        assertEquals(new GenericEntity<>(List.of(), CallableResourceMethods.class.getMethod("getList").getGenericReturnType()), resourceMethod.call(context, builder));
    }
    @Test
    public void should_call_resource_method_with_void_return_type() throws NoSuchMethodException {
        DefaultResourceMethod resourceMethod = getResourceMethod("post");

        assertNull(resourceMethod.call(context, builder));
    }
    record InjectableTypeTestCase(Class<?> type, String name, Object value){}
    @TestFactory
    public List<DynamicTest> inject_convertable_types(){
        List<DynamicTest> tests = new ArrayList<>();
        List<InjectableTypeTestCase> typeCases = List.of(
                new InjectableTypeTestCase(String.class, "string", "string"),
                new InjectableTypeTestCase(int.class, "1", 1),
                new InjectableTypeTestCase(double.class, "3.25", 3.25),
                new InjectableTypeTestCase(short.class, "128", (short)128),
                new InjectableTypeTestCase(byte.class, "42", (byte)42),
                new InjectableTypeTestCase(boolean.class, "true", true),
                new InjectableTypeTestCase(BigDecimal.class, "123456", new BigDecimal("123456")),
                new InjectableTypeTestCase(Convert.class, "Factory", Convert.Factory)
        );
        List<String> paramTypes =List.of("getPathParam", "getQueryParam");

        for (String type: paramTypes) {
            for (InjectableTypeTestCase testCase: typeCases) {
                tests.add(DynamicTest.dynamicTest("should inject " + testCase.type.getSimpleName() + " to " + type, () -> {
                    verifyResourceMethodCalled(type, testCase.type, testCase.name, testCase.value);
                }));
            }
        }

        return tests;
    }
    @TestFactory
    public List<DynamicTest> inject_context_object(){
        List<DynamicTest> tests = new ArrayList<>();
        List<InjectableTypeTestCase> typeCases = List.of(
                new InjectableTypeTestCase(SomeServiceInContext.class, "N/A", service),
                new InjectableTypeTestCase(ResourceContext.class, "N/A", context),
                new InjectableTypeTestCase(UriInfo.class, "N/A", uriInfo)
        );
        for (InjectableTypeTestCase testCase: typeCases) {
            tests.add(DynamicTest.dynamicTest("should inject " + testCase.type.getSimpleName() + " to Context", () -> {
                verifyResourceMethodCalled("getContext", testCase.type, testCase.name, testCase.value);
            }));
        }
        return tests;
    }

    //TODO: using default convertors for matrix, query, form, header, cookie
    //TODO: default convertors for List, Set, SortSet, Array
    private void verifyResourceMethodCalled(String method, Class<?> type, String paramString, Object paramValue) throws NoSuchMethodException {
        DefaultResourceMethod resourceMethod = getResourceMethod(method, type);
        parameters.put("param", List.of(paramString));
        resourceMethod.call(context, builder);

        assertEquals(getMethodName(method, List.of(type)), lastCal.name());
        assertEquals(List.of(paramValue), lastCal.arguments);
    }
    private DefaultResourceMethod getResourceMethod(String method, Class... types) throws NoSuchMethodException {
        return new DefaultResourceMethod(CallableResourceMethods.class.getMethod(method, types));
    }

    interface CallableResourceMethods {
        @GET
        String get();

        @POST
        void post();

        @GET
        List<String> getList();

        @GET
        String getPathParam(@PathParam("param") String value);
        @GET
        String getPathParam(@PathParam("param") int value);
        @GET
        String getPathParam(@PathParam("param") double value);
        @GET
        String getPathParam(@PathParam("param") short value);
        @GET
        String getPathParam(@PathParam("param") byte value);
        @GET
        String getPathParam(@PathParam("param") boolean value);
        @GET
        String getPathParam(@PathParam("param") BigDecimal value);
        @GET
        String getPathParam(@PathParam("param") Convert value);
        @GET
        String getContext(@Context SomeServiceInContext service);
        @GET
        String getContext(@Context ResourceContext context);
        @GET
        String getContext(@Context UriInfo uriInfo);
        @GET
        String getQueryParam(@QueryParam("param") String value);
        @GET
        String getQueryParam(@QueryParam("param") int value);
        @GET
        String getQueryParam(@QueryParam("param") double value);
        @GET
        String getQueryParam(@QueryParam("param") short value);
        @GET
        String getQueryParam(@QueryParam("param") byte value);
        @GET
        String getQueryParam(@QueryParam("param") boolean value);
        @GET
        String getQueryParam(@QueryParam("param") BigDecimal value);
        @GET
        String getQueryParam(@QueryParam("param") Convert value);
    }

    record LastCal(String name, List<Object> arguments) {}
}
enum Convert{
    Primitive, Constructor, Factory
}
interface SomeServiceInContext{

}

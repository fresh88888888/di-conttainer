package org.ws.tdd.rest;

import jakarta.ws.rs.*;
import jakarta.ws.rs.container.ResourceContext;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.UriInfo;
import org.junit.jupiter.api.*;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SubResourceLocatorTest {
    private ResourceContext context;
    private UriInfoBuilder builder;
    private SubResourceMethods resource;
    private LastCal lastCal;
    private UriInfo uriInfo;
    private UriTemplate.MatchResult result;
    private MultivaluedHashMap<String, String> parameters;
    record LastCal(String name, List<Object> arguments) {}

    @BeforeEach
    public void setup(){
        lastCal = null;
        resource = (SubResourceMethods) Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[]{SubResourceMethods.class}, (proxy, method, args) -> {
            lastCal = new LastCal(getMethodName(method.getName(), Arrays.stream(method.getParameters()).map(Parameter::getType).toList()), args != null ? List.of(args) : List.of());
            return new Message();
        });

        context = mock(ResourceContext.class);
        builder = mock(UriInfoBuilder.class);
        uriInfo = mock(UriInfo.class);
        parameters = new MultivaluedHashMap<>();
        result = mock(UriTemplate.MatchResult.class);

        when(builder.getLastMatchedResource()).thenReturn(resource);
        when(builder.createUriInfo()).thenReturn(uriInfo);
        when(uriInfo.getPathParameters()).thenReturn(parameters);
        when(uriInfo.getQueryParameters()).thenReturn(parameters);
    }
    private static String getMethodName(String name, List<? extends Class<?>> classStream) {
        return name + "(" + classStream.stream().map(Class::getSimpleName).collect(Collectors.joining(",")) + ")";
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
                new InjectableTypeTestCase(BigDecimal.class, "123456", new BigDecimal("123456"))
        );
        List<String> paramTypes =List.of("getPathParam", "getQueryParam");

        for (String type: paramTypes) {
            for (InjectableTypeTestCase testCase: typeCases) {
                tests.add(DynamicTest.dynamicTest("should inject " + testCase.type.getSimpleName() + " to " + type, () -> {
                    verifySubResourceMethodCalled(type, testCase.type, testCase.name, testCase.value);
                }));
            }
        }

        return tests;
    }
    private void verifySubResourceMethodCalled(String methodName, Class<?> type, String paramString, Object paramValue) throws NoSuchMethodException {
        Method method = SubResourceMethods.class.getMethod(methodName, type);
        SubResourceLocators.SubResourceLocator locator = new SubResourceLocators.SubResourceLocator(method);
        parameters.put("param", List.of(paramString));
        locator.match(result, "GET", new String[]{MediaType.TEXT_PLAIN}, context, builder);

        assertEquals(getMethodName(methodName, List.of(type)), lastCal.name);
        assertEquals(List.of(paramValue), lastCal.arguments);
    }

    interface SubResourceMethods{
        @Path("/message/{param}")
        @Produces(MediaType.TEXT_PLAIN)
        Message getPathParam(@PathParam("param") String path);

        @Path("/message/{param}")
        @Produces(MediaType.TEXT_PLAIN)
        Message getPathParam(@PathParam("param") int path);

        @Path("/message/{param}")
        @Produces(MediaType.TEXT_PLAIN)
        Message getPathParam(@PathParam("param") double path);

        @Path("/message/{param}")
        @Produces(MediaType.TEXT_PLAIN)
        Message getPathParam(@PathParam("param") byte path);

        @Path("/message/{param}")
        @Produces(MediaType.TEXT_PLAIN)
        Message getPathParam(@PathParam("param") short path);

        @Path("/message/{param}")
        @Produces(MediaType.TEXT_PLAIN)
        Message getPathParam(@PathParam("param") boolean path);

        @Path("/message/{param}")
        @Produces(MediaType.TEXT_PLAIN)
        Message getPathParam(@PathParam("param") BigDecimal path);

        @Path("/message/{param}")
        @Produces(MediaType.TEXT_PLAIN)
        Message getQueryParam(@QueryParam("param") String path);

        @Path("/message/{param}")
        @Produces(MediaType.TEXT_PLAIN)
        Message getQueryParam(@QueryParam("param") int path);

        @Path("/message/{param}")
        @Produces(MediaType.TEXT_PLAIN)
        Message getQueryParam(@QueryParam("param") short path);

        @Path("/message/{param}")
        @Produces(MediaType.TEXT_PLAIN)
        Message getQueryParam(@QueryParam("param") double path);

        @Path("/message/{param}")
        @Produces(MediaType.TEXT_PLAIN)
        Message getQueryParam(@QueryParam("param") boolean path);

        @Path("/message/{param}")
        @Produces(MediaType.TEXT_PLAIN)
        Message getQueryParam(@QueryParam("param") byte path);

        @Path("/message/{param}")
        @Produces(MediaType.TEXT_PLAIN)
        Message getQueryParam(@QueryParam("param") BigDecimal path);
    }

    static class Message{
        @GET
        @Produces(MediaType.TEXT_PLAIN)
        public String content() {
            return "content";
        }
    }
}

package org.ws.tdd.rest;

import jakarta.servlet.Servlet;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.container.ResourceContext;
import jakarta.ws.rs.core.*;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.MessageBodyWriter;
import jakarta.ws.rs.ext.Providers;
import jakarta.ws.rs.ext.RuntimeDelegate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class IntegrationTest extends ServletTest{

    private Runtime runtime;
    private ResourceRouter router;
    private ResourceContext context;
    private Providers providers;
    private RuntimeDelegate delegate;
    private UriInfo uriInfo;

    @Override
    protected Servlet getServlet() {
        runtime = mock(Runtime.class);
        router = new DefaultResourceRouter(runtime, List.of(new ResourceHandler(UsersApi.class)));
        context = mock(ResourceContext.class);
        providers = mock(Providers.class);
        uriInfo = mock(UriInfo.class);

        when(runtime.getResourceRouter()).thenReturn(router);
        when(runtime.createUriInfoBuilder(any())).thenReturn(new StubUriInfoBuilder(uriInfo));
        when(runtime.createResourceContext(any(), any())).thenReturn(context);
        when(context.getResource(eq(UsersApi.class))).thenReturn(new UsersApi());
        when(runtime.getProviders()).thenReturn(providers);

        MultivaluedHashMap<String, String> parameters = new MultivaluedHashMap<>();
        parameters.put("id", List.of("zhang.san"));
        when(uriInfo.getPathParameters()).thenReturn(parameters);

        return new ResourceServlet(runtime);
    }

    @BeforeEach
    public void before() {
        delegate = mock(RuntimeDelegate.class);
        RuntimeDelegate.setInstance(delegate);

        when(delegate.createResponseBuilder()).thenReturn(new StubResponseBuilder());
        when(delegate.createHeaderDelegate(NewCookie.class)).thenReturn(new RuntimeDelegate.HeaderDelegate<>() {
            @Override
            public NewCookie fromString(String value) {
                return null;
            }

            @Override
            public String toString(NewCookie value) {
                return value.getName() + "=" + value.getValue();
            }
        });
        when(providers.getExceptionMapper(any())).thenReturn(new ExceptionMapper<Throwable>() {
            @Override
            public Response toResponse(Throwable exception) {
                exception.printStackTrace();
                return new StubResponseBuilder().status(500).build();
            }
        });
        when(providers.getMessageBodyWriter(eq(String.class), eq(String.class), any(), any())).thenReturn(new MessageBodyWriter<>() {
            @Override
            public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
                return false;
            }

            @Override
            public void writeTo(String s, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException {
                PrintWriter writer = new PrintWriter(entityStream);
                writer.write(s);
                writer.flush();
            }
        });
    }
    //TODO: get url (root/sub)
    @Test
    public void should_return_to_string_of_user_if_user_exist(){
        HttpResponse<String> response = get("/users/zhang.san");
        assertEquals(response.statusCode(), 200);
        assertEquals(response.body(), new User("zhang.san", new UserData("zhang.san", "zhang.san@foxmail.com.cn")).toString());
    }
    @Test
    public void should_return_404_if_url_in_exist(){
        HttpResponse<String> response = get("/customers");
        assertEquals(response.statusCode(), 404);
    }
    @Test
    public void should_return_404_if_user_not_exist(){
        HttpResponse<String> response = get("/users/lisi");
        assertEquals(response.statusCode(), 404);
    }
}
record UserData(String name, String email){
}
class User{
    private String id;
    private UserData data;

    public User(String id, UserData data) {
        this.id = id;
        this.data = data;
    }
    public String getId() {
        return id;
    }

    public UserData getData() {
        return data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public String toString() {
        return "User{" + "id='" + id + '\'' + '}';
    }
}
@Path("/users")
class UsersApi{
    private List<User> users;
    public UsersApi() {
        users = List.of(new User("zhang.san", new UserData("zhang.san", "zhang.san@foxmail.com.cn")));
    }

    @Path("/{id}")
    public UserApi findUserById(@PathParam("id") String id){
       return users.stream().filter(user -> user.getId().equals(id)).findFirst().map(UserApi::new).orElseThrow(() -> new WebApplicationException(404));
    }
}
class UserApi{
    private User user;

    public UserApi(User user) {
        this.user = user;
    }

    @GET
    public String get(){
        return user.toString();
    }
}
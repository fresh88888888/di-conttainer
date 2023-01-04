package org.ws.tdd.rest;

import jakarta.servlet.Servlet;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.container.ResourceContext;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Providers;
import jakarta.ws.rs.ext.RuntimeDelegate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.http.HttpResponse;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class IntegrationTest extends ServletTest{

    private Runtime runtime;
    private ResourceRouter router;
    private ResourceContext context;
    private Providers providers;
    private RuntimeDelegate delegate;

    @Override
    protected Servlet getServlet() {
        runtime = mock(Runtime.class);
        router = new DefaultResourceRouter(runtime, List.of(new ResourceHandler(UsersApi.class)));
        context = mock(ResourceContext.class);
        providers = mock(Providers.class);

        when(runtime.getResourceRouter()).thenReturn(router);
        when(runtime.createUriInfoBuilder(any())).thenReturn(new StubUriInfoBuilder());
        when(runtime.createResourceContext(any(), any())).thenReturn(context);
        when(runtime.getProviders()).thenReturn(providers);

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
    }
    //TODO: get url (root/sub)
    //TODO: get url throw exception
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
}
@Path("/users")
class UsersApi{
    private List<User> users;
    public UsersApi() {
        users = List.of(new User("zhang san", new UserData("zhang san", "zhang.san@foxmail.com.cn")));
    }

    @Path("{id}")
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
        return "";
    }
}

#### ResourceServlet
将请求派分给对应的资源（Resource），并根据返回的状态、超媒体类型、内容，响应 Http 请求
- 使用 OutboundResponse 的 status 作为 Http Response 的状态
- 使用 OutboundResponse 的 headers 作为 Http Response 的 Http Headers 
- 通过 MessageBodyWriter 将 OutboundResponse 的 GenericEntity 写回为 Body 
- 如果找不到对应的 MessageBodyWriter，则返回 500 族错误
当资源方法抛出异常时，根据异常影响 Http 请求
- 如果抛出 WebApplicationException，且 response 不为 null，则使用 response 响应 Http
- 如果抛出 WebApplicationException，而 response 为 null，则通过异常的具体类型查找 ExceptionMapper，生产 response 响应 Http 请求
- 如果抛出的不是 WebApplicationException，则通过异常的具体类型查找 ExceptionMapper，生产 response 响应 Http 请求
RuntimeDelegate
- 为 MediaType 提供 HeaderDelegate 
- 为 CacheControl 提供 HeaderDelegate 
- 为 Cookie 提供 HeaderDelegates 
- 为 EntityTag 提供 HeaderDelegate 
- 为 Link 提供 HeaderDelegate 
- 为 NewCookie 提供 HeaderDelegate 
- 为 Date 提供 HeaderDelegate
````java
package geektime.tdd.rest;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.RuntimeDelegate;
import java.io.IOException;

public class ResourceServlet extends HttpServlet {

    private Runtime runtime;
    
    public ResourceServlet(Runtime runtime) {
        this.runtime = runtime;
    }
    
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ResourceRouter router = runtime.getResourceRouter();
        OutboundResponse response = router.dispatch(req, runtime.createResourceContext(req, resp));
        resp.setStatus(response.getStatus());
        MultivaluedMap<String, Object> headers = response.getHeaders();
        for (String name : headers.keySet())
            for (Object value : headers.get(name)) {
                RuntimeDelegate.HeaderDelegate headerDelegate = RuntimeDelegate.getInstance().createHeaderDelegate(value.getClass());
                resp.addHeader(name, headerDelegate.toString(value));
            }
    }
}
````
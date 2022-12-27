
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
#### RuntimeDelegate
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
到目前为止，我们完成了第一层调用栈的测试。也就是以 ResourceServlet 为核心，测试驱动地实现了它与其他组件之间的交互。因为大量地使用测试替身（主要是 Stub），我们实际上围绕着 ResourceServlet 构建了一个抽象层。

如果我们继续沿着调用栈向内测试驱动，那么实际上就是为之前构建的抽象层提供了具体实现。因而，伦敦学派的过程就是一个从抽象到具体的测试驱动的过程。这也是为什么伦敦学派不惮于大量使用测试替身（甚至是 Mock）：具体实现是易变的，抽象是稳定的，因为它提炼了核心而忽略了细节。

如果抽象层构建合理，那么它就是稳定且不易改变的。重构和代码改写通常发生在实现层，合理的抽象可以屏蔽这些改变对于外界的影响。那么使用行为验证、mock、单元测试，也不会阻碍重构的进行。而随着调用栈向内，逐渐从抽象层走到具体实现的时候，具体的模块就不会再依赖额外的组件，那么单元测试自然变成状态验证的单元级别功能测试。

伦敦学派与经典学派具有完全不同的测试节奏。经典学派是从功能入手，完成功能之后，再通过重构做抽象与提炼。而伦敦学派则是从抽象入手，先构建一个抽象的机制（Abstraction Mechanism），再逐步具化抽象机制中的组件。

因而，伦敦学派的难点有两个：在调用栈外层的时候，如何构建足够好的抽象层，以屏蔽具体实现变化带来的影响；逐步深入调用栈时，如何选择恰当的抽象层级。过多的抽象会不断加深调用栈，让代码变得细碎且难理解。
在前面的课程中，我们展示了如何构建外层的抽象：通过 Spike 消除不确定性，从中提取架构愿景，并转化为抽象的接口。换句话说，我们使用了不严格的经典学派（没有大量的测试，架构愿景提取代替了测试），构建了伦敦学派的起点。
可以看到，列表中包含了抽象层中所有的组件，以及在最外层交互和测试的过程中识别的功能上下文，比如 ResourceDispatcher 按照 Reosurce Method 返回值来包装 Response 对象。这个时候，将要如何继续分解任务呢？ 一个简单的考量是，能不能直接进入经典模式继续开发。如果可以，比如 Runtimes、Providers、OutboundResponseBuilder 等，就直接分解任务。如果不能，比如 Resource Dispatcher，那么可以继续通过 Spike 消除不确定性，再一层抽象。
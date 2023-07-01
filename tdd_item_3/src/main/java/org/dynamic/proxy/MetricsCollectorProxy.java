package org.dynamic.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class MetricsCollectorProxy {
    private final MetricsCollector metricsCollector;

    public MetricsCollectorProxy() {
        this.metricsCollector = new MetricsCollector();
    }

    public Object createProxy(Object proxyObject){
        Class<?>[] interfaces = proxyObject.getClass().getInterfaces();
        DynamicProxyHandler handler = new DynamicProxyHandler(proxyObject);
        return Proxy.newProxyInstance(proxyObject.getClass().getClassLoader(), interfaces, handler);
    }

    private class DynamicProxyHandler implements InvocationHandler {
        private final Object proxyObject;

        private DynamicProxyHandler(Object proxyObject) {
            this.proxyObject = proxyObject;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            long startTimestamp = System.currentTimeMillis();
            Object result = method.invoke(proxyObject, args);
            long endTimestamp = System.currentTimeMillis();
            long responseTime = endTimestamp - startTimestamp;
            String apiName = proxyObject.getClass().getName() + ":" + method.getName();
            RequestInfo requestInfo = new RequestInfo(apiName, responseTime, startTimestamp);
            metricsCollector.recordRequest(requestInfo);
            return result;
        }
    }
}

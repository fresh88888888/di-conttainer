package org.dynamic.proxy;

public class RequestInfo {
    private String apiName;
    private long responseTime;
    private long startTimeStamp;

    public RequestInfo(String apiName, long responseTime, long startTimeStamp) {
        this.apiName = apiName;
        this.responseTime = responseTime;
        this.startTimeStamp = startTimeStamp;
    }
}

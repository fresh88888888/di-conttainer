package org.ws.tdd.rest;

interface UriInfoBuilder {
    void pushMatchedPath(String path);

    void addParameter(String name, String value);

    String getUnmatchedPath();
}

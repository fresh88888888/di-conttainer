package org.ws.tdd.rest;

interface UriInfoBuilder {
    Object getLastMatchedResource();
    void addMatchedResource(Object resource);
}

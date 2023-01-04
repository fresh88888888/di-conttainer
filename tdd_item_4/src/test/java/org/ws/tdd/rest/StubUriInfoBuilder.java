package org.ws.tdd.rest;

import jakarta.ws.rs.core.*;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class StubUriInfoBuilder implements UriInfoBuilder {
    private List<Object> matchedResults = new ArrayList<>();
    private MultivaluedMap<String, String> pathParameters = new MultivaluedHashMap<>();
    private UriInfo uriInfo;
    public StubUriInfoBuilder(UriInfo uriInfo) {
        this.uriInfo = uriInfo;
    }

    public StubUriInfoBuilder() {
    }

    @Override
    public Object getLastMatchedResource() {
        return matchedResults.get(matchedResults.size() - 1);
    }

    @Override
    public void addMatchedResource(Object resource) {
        matchedResults.add(resource);
    }

    @Override
    public void addMatchedPathParameters(Map<String, String> pathParameters) {
        for (String key: pathParameters.keySet()) {
            this.pathParameters.add(key, pathParameters.get(key));
        }
    }
    public MultivaluedMap<String, String> getPathParameters() {
        return pathParameters;
    }

    @Override
    public UriInfo createUriInfo() {
        return new UriInfo() {
            @Override
            public String getPath() {
                return null;
            }

            @Override
            public String getPath(boolean decode) {
                return null;
            }

            @Override
            public List<PathSegment> getPathSegments() {
                return null;
            }

            @Override
            public List<PathSegment> getPathSegments(boolean decode) {
                return null;
            }

            @Override
            public URI getRequestUri() {
                return null;
            }

            @Override
            public UriBuilder getRequestUriBuilder() {
                return null;
            }

            @Override
            public URI getAbsolutePath() {
                return null;
            }

            @Override
            public UriBuilder getAbsolutePathBuilder() {
                return null;
            }

            @Override
            public URI getBaseUri() {
                return null;
            }

            @Override
            public UriBuilder getBaseUriBuilder() {
                return null;
            }

            @Override
            public MultivaluedMap<String, String> getPathParameters() {
                return pathParameters ;
            }

            @Override
            public MultivaluedMap<String, String> getPathParameters(boolean decode) {
                return pathParameters;
            }

            @Override
            public MultivaluedMap<String, String> getQueryParameters() {
                return null;
            }

            @Override
            public MultivaluedMap<String, String> getQueryParameters(boolean decode) {
                return null;
            }

            @Override
            public List<String> getMatchedURIs() {
                return null;
            }

            @Override
            public List<String> getMatchedURIs(boolean decode) {
                return null;
            }

            @Override
            public List<Object> getMatchedResources() {
                return null;
            }

            @Override
            public URI resolve(URI uri) {
                return null;
            }

            @Override
            public URI relativize(URI uri) {
                return null;
            }
        };
    }
}

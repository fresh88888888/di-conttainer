package org.ws.tdd.rest;

import java.util.ArrayList;
import java.util.List;

class StubUriInfoBuilder implements UriInfoBuilder {
    private List<Object> matchedResults = new ArrayList<>();

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
}

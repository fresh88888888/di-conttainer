package org.ws.tdd.rest;

import jakarta.ws.rs.core.*;
import jakarta.ws.rs.ext.Providers;

import java.lang.annotation.Annotation;
import java.net.URI;
import java.util.*;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class StubResponseBuilder extends Response.ResponseBuilder {
    private GenericEntity<Object> entity;
    private Response.Status status = Response.Status.OK;
    private MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
    private Providers providers = mock(Providers.class);
    private Annotation[] annotations = new Annotation[0];
    private MediaType mediaType = MediaType.TEXT_PLAIN_TYPE;
    private Set<String> allowed = new HashSet<>();

    @Override
    public Response build() {
        OutboundResponse response = mock(OutboundResponse.class);
        when(response.getGenericEntity()).thenReturn(entity);
        when(response.getStatus()).thenReturn(status.getStatusCode());
        when(response.getAllowedMethods()).thenReturn(allowed);
        when(response.getStatusInfo()).thenReturn(status);
        when(response.getAnnotations()).thenReturn(annotations);
        when(response.getMediaType()).thenReturn(mediaType);
        when(response.getHeaders()).thenReturn(new MultivaluedHashMap<>());
        return response;
    }

    @Override
    public Response.ResponseBuilder clone() {
        return this;
    }

    @Override
    public Response.ResponseBuilder status(int status) {
        return this;
    }

    @Override
    public Response.ResponseBuilder status(int status, String reasonPhrase) {
        this.status = Response.Status.fromStatusCode(status);
        return this;
    }

    @Override
    public Response.ResponseBuilder entity(Object entity) {
        this.entity = (GenericEntity<Object>) entity;
        return this;
    }

    @Override
    public Response.ResponseBuilder entity(Object entity, Annotation[] annotations) {
        return this;
    }

    @Override
    public Response.ResponseBuilder allow(String... methods) {
        return this;
    }

    @Override
    public Response.ResponseBuilder allow(Set<String> methods) {
        allowed.addAll(methods);
        return this;
    }

    @Override
    public Response.ResponseBuilder cacheControl(CacheControl cacheControl) {
        return this;
    }

    @Override
    public Response.ResponseBuilder encoding(String encoding) {
        return this;
    }

    @Override
    public Response.ResponseBuilder header(String name, Object value) {
        this.headers.add(name, value);
        return this;
    }

    @Override
    public Response.ResponseBuilder replaceAll(MultivaluedMap<String, Object> headers) {
        return this;
    }

    @Override
    public Response.ResponseBuilder language(String language) {
        return this;
    }

    @Override
    public Response.ResponseBuilder language(Locale language) {
        return this;
    }

    @Override
    public Response.ResponseBuilder type(MediaType type) {
        return this;
    }

    @Override
    public Response.ResponseBuilder type(String type) {
        return this;
    }

    @Override
    public Response.ResponseBuilder variant(Variant variant) {
        return this;
    }

    @Override
    public Response.ResponseBuilder contentLocation(URI location) {
        return this;
    }

    @Override
    public Response.ResponseBuilder cookie(NewCookie... cookies) {
        return this;
    }

    @Override
    public Response.ResponseBuilder expires(Date expires) {
        return this;
    }

    @Override
    public Response.ResponseBuilder lastModified(Date lastModified) {
        return this;
    }

    @Override
    public Response.ResponseBuilder location(URI location) {
        return this;
    }

    @Override
    public Response.ResponseBuilder tag(EntityTag tag) {
        return this;
    }

    @Override
    public Response.ResponseBuilder tag(String tag) {
        return this;
    }

    @Override
    public Response.ResponseBuilder variants(Variant... variants) {
        return this;
    }

    @Override
    public Response.ResponseBuilder variants(List<Variant> variants) {
        return this;
    }

    @Override
    public Response.ResponseBuilder links(Link... links) {
        return this;
    }

    @Override
    public Response.ResponseBuilder link(URI uri, String rel) {
        return this;
    }

    @Override
    public Response.ResponseBuilder link(String uri, String rel) {
        return this;
    }
}

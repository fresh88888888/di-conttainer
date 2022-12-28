package org.ws.tdd.rest;

import jakarta.ws.rs.core.*;
import jakarta.ws.rs.ext.Providers;

import java.lang.annotation.Annotation;
import java.net.URI;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class StubResponseBuilder extends Response.ResponseBuilder {
    private GenericEntity<Object> entity = new GenericEntity<>("matched", String.class);
    private Response.Status status = Response.Status.OK;
    private MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
    private Providers providers = mock(Providers.class);
    private Annotation[] annotations = new Annotation[0];
    private MediaType mediaType = MediaType.TEXT_PLAIN_TYPE;

    @Override
    public Response build() {
        OutboundResponse response = mock(OutboundResponse.class);
        when(response.getGenericEntity()).thenReturn(entity);
        when(response.getStatus()).thenReturn(status.getStatusCode());
        when(response.getStatusInfo()).thenReturn(status);
        when(response.getAnnotations()).thenReturn(annotations);
        when(response.getMediaType()).thenReturn(mediaType);
        return response;
    }

    @Override
    public Response.ResponseBuilder clone() {
        return null;
    }

    @Override
    public Response.ResponseBuilder status(int status) {
        return null;
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
        return null;
    }

    @Override
    public Response.ResponseBuilder allow(String... methods) {
        return null;
    }

    @Override
    public Response.ResponseBuilder allow(Set<String> methods) {
        return null;
    }

    @Override
    public Response.ResponseBuilder cacheControl(CacheControl cacheControl) {
        return null;
    }

    @Override
    public Response.ResponseBuilder encoding(String encoding) {
        return null;
    }

    @Override
    public Response.ResponseBuilder header(String name, Object value) {
        this.headers.add(name, value);
        return this;
    }

    @Override
    public Response.ResponseBuilder replaceAll(MultivaluedMap<String, Object> headers) {
        return null;
    }

    @Override
    public Response.ResponseBuilder language(String language) {
        return null;
    }

    @Override
    public Response.ResponseBuilder language(Locale language) {
        return null;
    }

    @Override
    public Response.ResponseBuilder type(MediaType type) {
        return null;
    }

    @Override
    public Response.ResponseBuilder type(String type) {
        return null;
    }

    @Override
    public Response.ResponseBuilder variant(Variant variant) {
        return null;
    }

    @Override
    public Response.ResponseBuilder contentLocation(URI location) {
        return null;
    }

    @Override
    public Response.ResponseBuilder cookie(NewCookie... cookies) {
        return null;
    }

    @Override
    public Response.ResponseBuilder expires(Date expires) {
        return null;
    }

    @Override
    public Response.ResponseBuilder lastModified(Date lastModified) {
        return null;
    }

    @Override
    public Response.ResponseBuilder location(URI location) {
        return null;
    }

    @Override
    public Response.ResponseBuilder tag(EntityTag tag) {
        return null;
    }

    @Override
    public Response.ResponseBuilder tag(String tag) {
        return null;
    }

    @Override
    public Response.ResponseBuilder variants(Variant... variants) {
        return null;
    }

    @Override
    public Response.ResponseBuilder variants(List<Variant> variants) {
        return null;
    }

    @Override
    public Response.ResponseBuilder links(Link... links) {
        return null;
    }

    @Override
    public Response.ResponseBuilder link(URI uri, String rel) {
        return null;
    }

    @Override
    public Response.ResponseBuilder link(String uri, String rel) {
        return null;
    }
}

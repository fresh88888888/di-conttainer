package org.ws.tdd.rest;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class UriTemplateStringTest {

    @Test
    public void should_return_empty_if_path_not_matched() {
        UriTemplateString template = new UriTemplateString("/users");

        assertTrue(template.match("/orders").isEmpty());
    }
    @Test
    public void should_return_match_result_if_path_matched(){
        UriTemplateString template = new UriTemplateString("/users");
        Optional<UriTemplate.MatchResult> match = template.match("/users/1");

        assertEquals("/users", match.get().getMatched());
        assertEquals("/1", match.get().getRemaining());
        assertTrue(match.get().getMatchedPathParameters().isEmpty());
    }
    @Test
    public void should_return_match_result_if_matched_with_variable(){
        UriTemplateString template = new UriTemplateString("/users/{id}");
        Optional<UriTemplate.MatchResult> match = template.match("/users/1");

        assertEquals("/users/1", match.get().getMatched());
        assertNull(match.get().getRemaining());
        assertFalse(match.get().getMatchedPathParameters().isEmpty());
        assertEquals("1", match.get().getMatchedPathParameters().get("id"));
    }
    @Test
    public void should_return_empty_if_not_match_given_pattern(){
        UriTemplateString template = new UriTemplateString("/users/{id:[0-9]+}");
        assertTrue(template.match("/orders/id").isEmpty());
    }
    @Test
    public void should_extract_variable_value_by_given_pattern(){
        UriTemplateString template = new UriTemplateString("/users/{id:[0-9]+}");
        UriTemplate.MatchResult result = template.match("/users/1").get();

        assertEquals("1", result.getMatchedPathParameters().get("id"));
    }
    @Test
    public void should_throw_illegal_argument_exception_if_variable_redefined(){
        assertThrows(IllegalArgumentException.class, () -> new UriTemplateString("/users/{id:[0-9]+}/{id}"));
    }
    //TODO: throw exception if variable redefined
    //TODO: comparing result, with match literal, variable and specific variable
}

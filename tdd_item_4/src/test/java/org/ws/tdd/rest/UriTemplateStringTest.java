package org.ws.tdd.rest;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class UriTemplateStringTest {

    //TODO: if path not match uri template ,return empty
    @Test
    public void should_return_empty_if_path_not_matched() {
        UriTemplateString template = new UriTemplateString("/users");
        Optional<UriTemplate.MatchResult> match = template.match("/orders");

        assertTrue(match.isEmpty());
    }
    //TODO: if path match uri template , return matched result
    //TODO: path match with variable
    //TODO: path match with variable with specific pattern
    //TODO: throw exception if variable undefined
    //TODO: comparing result, with match literal, variable and specific variable
}

package org.ws.tdd.rest;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UriTemplateStringTest {

    @Test
    public void should_return_empty_if_path_not_matched() {
        UriTemplateString template = new UriTemplateString("/users");
        Optional<UriTemplate.MatchResult> match = template.match("/orders");

        assertTrue(match.isEmpty());
    }
    @Test
    public void should_return_match_result_if_path_matched(){
        UriTemplateString template = new UriTemplateString("/users");
        Optional<UriTemplate.MatchResult> match = template.match("/users/1");

        assertEquals("/users", match.get().getMatched());
        assertEquals("/1", match.get().getRemaining());
    }
    //TODO: path match with variable
    //TODO: path match with variable with specific pattern
    //TODO: throw exception if variable undefined
    //TODO: comparing result, with match literal, variable and specific variable
}

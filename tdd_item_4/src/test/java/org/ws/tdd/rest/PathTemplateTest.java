package org.ws.tdd.rest;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class PathTemplateTest {
    @ParameterizedTest
    @CsvSource({"/users, /orders", "/users/{id:[0-9]+}, /orders/id", "/users, /unit/users"})
    public void should_not_match_path(String pattern, String path){
        PathTemplate template = new PathTemplate(pattern);
        assertTrue(template.match(path).isEmpty());
    }
    @Test
    public void should_return_match_result_if_path_matched(){
        PathTemplate template = new PathTemplate("/users");
        Optional<UriTemplate.MatchResult> match = template.match("/users/1");

        assertEquals("/users", match.get().getMatched());
        assertEquals("/1", match.get().getRemaining());
        assertTrue(match.get().getMatchedPathParameters().isEmpty());
    }
    @Test
    public void should_return_match_result_if_matched_with_variable(){
        PathTemplate template = new PathTemplate("/users/{id}");
        Optional<UriTemplate.MatchResult> match = template.match("/users/1");

        assertEquals("/users/1", match.get().getMatched());
        assertNull(match.get().getRemaining());
        assertFalse(match.get().getMatchedPathParameters().isEmpty());
        assertEquals("1", match.get().getMatchedPathParameters().get("id"));
    }

    @Test
    public void should_extract_variable_value_by_given_pattern(){
        PathTemplate template = new PathTemplate("/users/{id:[0-9]+}");
        UriTemplate.MatchResult result = template.match("/users/1").get();

        assertEquals("1", result.getMatchedPathParameters().get("id"));
    }
    @ParameterizedTest
    @CsvSource({"/users/1234, /users/1234, /users/{id}", "/users/1234567890/order,/{resources}/1234567890/{action},/users/{id}/order", "/users/1,/users/{id:[0-9]+},/users/{id}"})
    public void should(String path, String smallerTemplate, String largerTemplate){
        UriTemplate smaller = new PathTemplate(smallerTemplate);
        UriTemplate larger = new PathTemplate(largerTemplate);
        UriTemplate.MatchResult lhs = smaller.match(path).get();
        UriTemplate.MatchResult rhs = larger.match(path).get();

        assertTrue(lhs.compareTo(rhs) < 0);
        assertTrue(rhs.compareTo(lhs) > 0);
    }
    @Test
    public void should_throw_illegal_argument_exception_if_variable_redefined(){
        assertThrows(IllegalArgumentException.class, () -> new PathTemplate("/users/{id:[0-9]+}/{id}"));
    }
    @Test
    public void should_compare_equal_match_result(){
        UriTemplate template = new PathTemplate("/users/{id}");
        UriTemplate.MatchResult result = template.match("/users/1").get();

        assertEquals(0, result.compareTo(result));
    }
}

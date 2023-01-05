package org.ws.tdd.rest;

import java.util.Map;
import java.util.Optional;

interface UriTemplate {
    Optional<MatchResult> match(String path);
    interface MatchResult extends Comparable<MatchResult> {
        String getMatched();
        String getRemaining();
        Map<String, String> getMatchedPathParameters();
    }
}


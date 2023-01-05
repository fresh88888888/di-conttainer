package org.ws.tdd.rest;

import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

interface UriHandler {
    UriTemplate getUriTemplate();
}


package org.ws.tdd.rest;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class ConverterConstructorTest {
    @Test
    public void should_convert_via_convert_constructor(){
        assertEquals(Optional.of(new BigDecimal("123456")), ConverterConstructor.convert(BigDecimal.class, "123456"));
    }
}

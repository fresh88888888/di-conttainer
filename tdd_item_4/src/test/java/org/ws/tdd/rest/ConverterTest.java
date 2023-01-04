package org.ws.tdd.rest;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ConverterTest {
    @Test
    public void should_convert_via_convert_constructor(){
        assertEquals(Optional.of(new BigDecimal("123456")), ConverterConstructor.convert(BigDecimal.class, "123456"));
    }
    @Test
    public void should__not_convert_if_no_convert_constructor(){
        assertEquals(Optional.empty(), ConverterConstructor.convert(NoConverter.class, "123456"));
    }
    @Test
    public void should_not_convert_if_no_convert_factory(){
        assertEquals(Optional.empty(), ConverterFactory.convert(NoConverter.class, "12345"));
    }

    @Test
    public void should_convert_via_convert_factory(){
        assertEquals(Optional.of(InjectableCallerTest.Convert.Factory), ConverterFactory.convert(DefaultResourceMethodTest.Convert.class, "Factory"));
    }
}
class NoConverter{
    NoConverter valueOf(String value){
        return new NoConverter();
    }
}
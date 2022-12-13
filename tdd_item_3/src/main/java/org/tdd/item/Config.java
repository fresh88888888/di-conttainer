package org.tdd.item;

import java.lang.annotation.*;

public interface Config {
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface Export {
        Class<?> value();
    }
}

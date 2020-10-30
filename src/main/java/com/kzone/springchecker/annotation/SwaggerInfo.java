package com.kzone.springchecker.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface SwaggerInfo {

    String title();
    String description() default "";
    String version();
    Servers [] servers();

}

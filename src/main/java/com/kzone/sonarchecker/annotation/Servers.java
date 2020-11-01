package com.kzone.sonarchecker.annotation;

public @interface Servers {

    String url();
    String description() default "";

}

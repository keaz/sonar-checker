package com.kzone.springchecker.annotation;

public @interface Servers {

    String url();
    String description() default "";

}

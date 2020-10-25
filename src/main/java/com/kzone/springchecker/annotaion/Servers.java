package com.kzone.springchecker.annotaion;

public @interface Servers {

    String url();
    String description() default "";

}

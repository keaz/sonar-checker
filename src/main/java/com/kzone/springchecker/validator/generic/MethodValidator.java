package com.kzone.springchecker.validator.generic;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.util.ElementKindVisitor8;
import java.util.function.Predicate;

public class MethodValidator {

    public static final Predicate<? super Element> METHOD_FILTER = element -> element.getKind() == ElementKind.METHOD;

    public void validateMethodBody(Element element) {

//        ExecutableType method = (ExecutableType) element.asType();
//        System.out.println("Method elements" + method);
    }

}

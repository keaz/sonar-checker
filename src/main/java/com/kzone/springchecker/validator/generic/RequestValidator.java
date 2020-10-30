package com.kzone.springchecker.validator.generic;

import com.google.auto.service.AutoService;
import com.kzone.springchecker.validator.BaseValidator;

import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.tools.Diagnostic;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.kzone.springchecker.validator.generic.FieldValidator.CONSTANT_FILTER;
import static com.kzone.springchecker.validator.generic.FieldValidator.FIELD_FILTER;

@SupportedAnnotationTypes({"com.kzone.springchecker.annotation.Request"})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@AutoService(Processor.class)
public class RequestValidator extends BaseValidator {

    Logger logger = Logger.getLogger(RequestValidator.class.getName());
    private final FieldValidator fieldValidator = new FieldValidator();
    private final MethodValidator methodValidator = new MethodValidator();

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Optional<? extends TypeElement> first = annotations.stream().findFirst();
        if (first.isPresent()) {
            Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(first.get());
            elements.forEach(this::checkElement);
        }
        return true;
    }

    private void checkElement(Element element) {
        List<? extends Element> enclosedElements = element.getEnclosedElements();
        Set<? extends Element> instanceFieldSet = enclosedElements.stream().
                filter(FIELD_FILTER).collect(Collectors.toSet());
        instanceFieldSet.forEach(field -> fieldValidator.validateFieldName(field,messager));

        Set<? extends Element> constantSet = enclosedElements.stream().
                filter(CONSTANT_FILTER).collect(Collectors.toSet());
        constantSet.forEach(constant -> fieldValidator.validateConstantName(constant,messager));

        Set<? extends Element> methods = enclosedElements.stream().filter(MethodValidator.METHOD_FILTER).collect(Collectors.toSet());
        System.out.println("*********  Methods "+ methods);
        methods.forEach(method -> methodValidator.validateMethodBody(method));

        List<? extends AnnotationMirror> annotationMirrors = element.getAnnotationMirrors();

    }


}

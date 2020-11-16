package com.kzone.sonarchecker.validator.spring;

import com.google.auto.service.AutoService;
import com.kzone.sonarchecker.validator.BaseValidator;

import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.tools.Diagnostic;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;

@SupportedAnnotationTypes({"org.springframework.web.bind.annotation.RestController"})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@AutoService(Processor.class)
public class RestControllerValidator extends BaseValidator {

    private final Logger logger = Logger.getLogger(RestControllerValidator.class.getName());

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

        if (element.getKind() == ElementKind.CLASS) {
            List<? extends AnnotationMirror> annotationMirrors = element.getAnnotationMirrors();
            Optional<? extends AnnotationMirror> first = annotationMirrors.stream().
                    filter(annotation -> annotation.getAnnotationType().toString().equals("org.springframework.web.bind.annotation.RequestMapping")).findFirst();

            if (!first.isPresent()) {
                messager.printMessage(Diagnostic.Kind.ERROR, "RestController is not annotated with org.springframework.web.bind.annotation.RequestMapping", element);
            }

            AnnotationMirror annotationMirror = first.get();
            Map<? extends ExecutableElement, ? extends AnnotationValue> elementValues = annotationMirror.getElementValues();

            Optional<? extends ExecutableElement> value = elementValues.keySet().stream().filter(exEl -> exEl.toString().equals("value()")).findFirst();
            if (!value.isPresent()) {
                messager.printMessage(Diagnostic.Kind.ERROR, "Value is null for org.springframework.web.bind.annotation.RequestMapping", element);
            }

            Object requestMappingValue = elementValues.get(value.get()).getValue();
            if (requestMappingValue.toString().isEmpty()) {
                messager.printMessage(Diagnostic.Kind.ERROR, "Value is empty for org.springframework.web.bind.annotation.RequestMapping", element);
            }

        }

    }

}

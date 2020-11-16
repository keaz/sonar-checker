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
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;

@SupportedAnnotationTypes({"org.springframework.beans.factory.annotation.Autowired"})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@AutoService(Processor.class)
public class AutowiredValidator extends BaseValidator {

    Logger logger = Logger.getLogger(AutowiredValidator.class.getName());

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
        List<? extends AnnotationMirror> annotationMirrors = element.getAnnotationMirrors();
        AnnotationMirror autowired = annotationMirrors.stream().filter(annotation ->
                annotation.getAnnotationType().toString().equals("org.springframework.beans.factory.annotation.Autowired"))
                .findFirst().get();
        if (element.getKind() == ElementKind.METHOD) {
            messager.printMessage(Diagnostic.Kind.ERROR, "Autowired is not allowed on methods", element,autowired);
        }
        if (element.getKind() == ElementKind.FIELD) {
            messager.printMessage(Diagnostic.Kind.ERROR, "Autowired is not allowed on fields", element,autowired);
        }

    }

}

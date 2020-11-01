package com.kzone.sonarchecker.validator.generic;

import com.google.auto.service.AutoService;
import com.kzone.sonarchecker.validator.BaseValidator;
import com.sun.source.util.Trees;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;

@SupportedAnnotationTypes({"com.kzone.springchecker.annotation.Request"})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@AutoService(Processor.class)
public class RequestValidator extends BaseValidator {

    Logger logger = Logger.getLogger(RequestValidator.class.getName());
    private final FieldValidator fieldValidator = new FieldValidator();
    private final MethodValidator methodValidator = new MethodValidator();
    Trees trees = null;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        trees = Trees.instance(processingEnv);

    }

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

        List<? extends AnnotationMirror> annotationMirrors = element.getAnnotationMirrors();

    }

}

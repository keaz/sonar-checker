package com.kzone.sonarchecker.validator.spring;

import com.google.auto.service.AutoService;
import com.kzone.sonarchecker.validator.BaseValidator;

import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

@SupportedAnnotationTypes({"org.springframework.web.bind.annotation.RequestMapping"})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@AutoService(Processor.class)
public class RequestMappingValidator extends BaseValidator {

    private final Logger logger = Logger.getLogger(RequestMappingValidator.class.getName());

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        logger.log(Level.CONFIG, () -> "Validating annotations :: " + annotations);
        Optional<? extends TypeElement> first = annotations.stream().findFirst();
        if (first.isPresent()) {
            Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(first.get());
            logger.log(Level.CONFIG, () -> "Elements annotated with :: " + elements);
            elements.forEach(this::checkElement);
        }
        return true;
    }

    private void checkElement(Element element) {

        if (element.getKind() == ElementKind.METHOD) {
            messager.printMessage(Diagnostic.Kind.ERROR, "RequestMapping annotation is not applicable to method", element);
        }

    }

}

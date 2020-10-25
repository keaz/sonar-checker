package com.kzone.springchecker.validator;

import com.google.auto.service.AutoService;
import com.kzone.springchecker.annotaion.SwaggerInfo;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

@SupportedAnnotationTypes({"com.kzone.swagger.annotaion.*"})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@AutoService(Processor.class)
public class BuilderProcessor extends BaseValidator {

    Logger logger = Logger.getLogger(BuilderProcessor.class.getName());

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (TypeElement annotation : annotations) {
            wrapAnnotation(annotation, roundEnv);
        }

        return true;
    }

    private void wrapAnnotation(TypeElement annotation, RoundEnvironment roundEnv) {
        Set<? extends Element> elementsAnnotatedWith = roundEnv.getElementsAnnotatedWith(annotation);
        //# FIX for now we select first element. Should fix to get class having the main method.
        Optional<? extends Element> firstSwaggerInfo = elementsAnnotatedWith.stream().filter(element -> element.getAnnotation(SwaggerInfo.class) != null).findFirst();
        if (firstSwaggerInfo.isPresent()) {
            Element swaggerInfoElement = firstSwaggerInfo.get();
            SwaggerInfo swaggerInfo = swaggerInfoElement.getAnnotation(SwaggerInfo.class);
            if (Objects.isNull(swaggerInfo.version()) || swaggerInfo.version().isEmpty()) {
                messager.printMessage(Diagnostic.Kind.ERROR, "@SwaggerInfo version is required", swaggerInfoElement);
            }
            return;
        }

    }

}

package com.kzone.springchecker.validator;

import com.google.auto.service.AutoService;

import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import java.util.Optional;
import java.util.Set;

@SupportedAnnotationTypes({"org.springframework.web.bind.annotation.PostMapping"})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@AutoService(Processor.class)
public class PostMappingValidator extends BaseValidator {


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

        if (element.getKind() == ElementKind.METHOD) { //Kind is always method.
            Set<Modifier> modifiers = element.getModifiers();
            checkModifiers(modifiers, Modifier.ABSTRACT, element);
            checkModifiers(modifiers, Modifier.PRIVATE, element);
            checkModifiers(modifiers, Modifier.STATIC, element);

            ExecutableType method = (ExecutableType) element.asType();
            TypeMirror returnType = method.getReturnType();
            checkReturnTypeIsVoid(returnType, element);
        }
    }

    private void checkModifiers(Set<Modifier> modifiers, Modifier modifier, Element element) {
        long count = modifiers.stream().filter(modifierToCheck -> modifierToCheck == modifier).count();
        if (count == 1) {
            messager.printMessage(Diagnostic.Kind.ERROR, modifier.toString().toLowerCase() + " method annotated as PostMapping ", element);
        }
    }

    private void checkReturnTypeIsVoid(TypeMirror returnType, Element element) {
        if (returnType.getKind() == TypeKind.VOID) {
            messager.printMessage(Diagnostic.Kind.ERROR, " Void is not applicable to method annotated as PostMapping ", element);
        }
    }

}

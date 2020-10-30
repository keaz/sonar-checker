package com.kzone.springchecker.validator.spring;

import com.google.auto.service.AutoService;
import com.kzone.springchecker.validator.BaseValidator;

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

@SupportedAnnotationTypes({"org.springframework.web.bind.annotation.PostMapping","org.springframework.web.bind.annotation.GetMapping",
        "org.springframework.web.bind.annotation.DeleteMapping","org.springframework.web.bind.annotation.PutMapping"})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@AutoService(Processor.class)
public class PostMappingValidator extends BaseValidator {

    private static final int GENERIC_WILD_CARD_INDEX = 40;

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        annotations.forEach(typeElement -> {
            Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(typeElement);
            elements.forEach(element -> checkElement(element,typeElement));
        });

        return true;
    }

    private void checkElement(Element element, TypeElement typeElement) {
        if (element.getKind() == ElementKind.METHOD) { //Kind is always method.
            Set<Modifier> modifiers = element.getModifiers();
            checkModifiers(modifiers, Modifier.ABSTRACT, element,typeElement);
            checkModifiers(modifiers, Modifier.PRIVATE, element,typeElement);
            checkModifiers(modifiers, Modifier.STATIC, element,typeElement);

            ExecutableType method = (ExecutableType) element.asType();
            TypeMirror returnType = method.getReturnType();
            checkReturnTypeIsVoid(returnType, element,typeElement);
        }
    }

    private void checkModifiers(Set<Modifier> modifiers, Modifier modifier, Element element,TypeElement typeElement) {
        long count = modifiers.stream().filter(modifierToCheck -> modifierToCheck == modifier).count();
        if (count == 1) {
            messager.printMessage(Diagnostic.Kind.ERROR, modifier.toString().toLowerCase() + " method annotated as "+typeElement, element);
        }
    }

    private void checkReturnTypeIsVoid(TypeMirror returnType, Element element,TypeElement typeElement) {
        if (returnType.getKind() == TypeKind.VOID) {
            messager.printMessage(Diagnostic.Kind.ERROR, " Void is not applicable to method annotated as "+typeElement, element);
            return;
        }

        //We cannot get the exact return type at the compile time. Expecting to return ResponseEntity
        String returnTypeString = returnType.toString();
        if(!returnTypeString.startsWith("org.springframework.http.ResponseEntity")){
            messager.printMessage(Diagnostic.Kind.ERROR, " Return type is not ResponseEntity on method annotated as "+typeElement, element);
        }

        if(returnTypeString.indexOf('?') == GENERIC_WILD_CARD_INDEX){
            messager.printMessage(Diagnostic.Kind.ERROR, " Generic wildcard types is returned on method annotated as "+typeElement, element);
        }
    }

}

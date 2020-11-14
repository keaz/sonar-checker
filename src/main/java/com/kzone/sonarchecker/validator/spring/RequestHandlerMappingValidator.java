package com.kzone.sonarchecker.validator.spring;

import com.google.auto.service.AutoService;
import com.kzone.sonarchecker.validator.BaseValidator;
import com.sun.tools.javac.code.Attribute;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;

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
import javax.tools.Diagnostic;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@SupportedAnnotationTypes({"org.springframework.web.bind.annotation.PostMapping", "org.springframework.web.bind.annotation.GetMapping",
        "org.springframework.web.bind.annotation.DeleteMapping", "org.springframework.web.bind.annotation.PutMapping"})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@AutoService(Processor.class)
public class RequestHandlerMappingValidator extends BaseValidator {

    private static final int GENERIC_WILD_CARD_INDEX = 40;

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        annotations.forEach(typeElement -> {
            Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(typeElement);
            elements.forEach(element -> checkElement(element, typeElement));
        });

        return true;
    }

    private void checkElement(Element element, TypeElement typeElement) {
        if (element.getKind() == ElementKind.METHOD) { //Kind is always method.
            Set<Modifier> modifiers = element.getModifiers();
            checkModifiers(modifiers, Modifier.ABSTRACT, element, typeElement);
            checkModifiers(modifiers, Modifier.PRIVATE, element, typeElement);
            checkModifiers(modifiers, Modifier.STATIC, element, typeElement);

            checkReturnType(element, typeElement);
            checkPersistenceEntityUsedAsArguments(element, typeElement);

        }
    }

    private void checkModifiers(Set<Modifier> modifiers, Modifier modifier, Element element, TypeElement typeElement) {
        long count = modifiers.stream().filter(modifierToCheck -> modifierToCheck == modifier).count();
        if (count == 1) {
            messager.printMessage(Diagnostic.Kind.ERROR, modifier.toString().toLowerCase() + " method annotated as " + typeElement, element);
        }
    }

    private void checkReturnType(Element element, TypeElement typeElement) {
        ExecutableType method = (ExecutableType) element.asType();
        Type.MethodType methodType = (Type.MethodType) method;
        Type returnType = methodType.getReturnType();

        if (returnType.getKind() == TypeKind.VOID) {
            messager.printMessage(Diagnostic.Kind.ERROR, " Void is not applicable to method annotated as " + typeElement, element);
            return;
        }

        //We cannot get the exact return type at the compile time. Expecting to return ResponseEntity
        String returnTypeString = returnType.toString();
        if (!returnTypeString.startsWith("org.springframework.http.ResponseEntity")) {
            messager.printMessage(Diagnostic.Kind.ERROR, " Return type is not ResponseEntity on method annotated as " + typeElement, element);
        }

        if (returnTypeString.indexOf('?') == GENERIC_WILD_CARD_INDEX) {
            messager.printMessage(Diagnostic.Kind.ERROR, " Generic wildcard types is returned on method annotated as " + typeElement, element);
            return;
        }

        checkPersistenceEntityUsedInReturn(element,typeElement,returnType);
    }

    private void checkPersistenceEntityUsedInReturn(Element element, TypeElement typeElement,Type returnType) {
        List<Type> allParams = returnType.baseType().allparams();
        if (!allParams.isEmpty()) {
            allParams.forEach(type -> {
                List<Attribute.Compound> declarationAttributes = type.asElement().getDeclarationAttributes();
                boolean hasEntityAnnotation = declarationAttributes.stream().anyMatch(compound -> compound.getAnnotationType().toString().equals("javax.persistence.Entity"));
                if (hasEntityAnnotation) {
                    messager.printMessage(Diagnostic.Kind.ERROR, " Persistent entities should not be used as return of " + typeElement.getQualifiedName() + " methods ", element);
                }
            });
        }
    }

    private void checkPersistenceEntityUsedAsArguments(Element element, TypeElement typeElement) {
        ExecutableType method = (ExecutableType) element.asType();
        Type.MethodType methodType = (Type.MethodType) method;
        List<Type> argTypes = methodType.argtypes;
        argTypes.forEach(arg -> {
            if (arg.asElement().getKind() == ElementKind.CLASS) {
                Symbol.ClassSymbol classSymbol = (Symbol.ClassSymbol) arg.asElement();
                List<Attribute.Compound> annotationMirrors = classSymbol.getAnnotationMirrors();
                Optional<Attribute.Compound> entityAnnotation = annotationMirrors.stream().filter(compound ->
                        compound.getAnnotationType().toString().equals("javax.persistence.Entity")).findFirst();
                if (entityAnnotation.isPresent()) {
                    messager.printMessage(Diagnostic.Kind.ERROR, " Persistent entities should not be used as arguments of " + typeElement.getQualifiedName() + " methods ", element);
                }

            }
        });

    }

}

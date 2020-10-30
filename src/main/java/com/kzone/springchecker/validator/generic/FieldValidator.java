package com.kzone.springchecker.validator.generic;

import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.tools.Diagnostic;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FieldValidator {

    private static final Pattern INSTANCE_FIELD_NAME_PATTERN = Pattern.compile("^[a-z][a-zA-Z0-9]*$");
    private static final Pattern CONSTANT_NAME_PATTERN = Pattern.compile("^[A-Z][A-Z0-9]*(_[A-Z0-9]+)*$");

    public static final Predicate<? super  Element> FIELD_FILTER = element -> element.getKind() == ElementKind.FIELD && !element.getModifiers().contains(Modifier.STATIC);
    public static final Predicate<? super  Element> CONSTANT_FILTER = element -> {
        Set<Modifier> modifiers = element.getModifiers();
        return element.getKind() == ElementKind.FIELD && modifiers.contains(Modifier.STATIC) && modifiers.contains(Modifier.FINAL);};

    public void validateFieldName(Element field, Messager messager) {
        String fieldName = field.getSimpleName().toString();
        Matcher matcher = INSTANCE_FIELD_NAME_PATTERN.matcher(fieldName);
        if (!matcher.find()) {
            messager.printMessage(Diagnostic.Kind.ERROR, "Field names is not comply with a naming convention", field);
        }
    }

    public void validateConstantName(Element field,Messager messager) {
        String fieldName = field.getSimpleName().toString();
        Matcher matcher = CONSTANT_NAME_PATTERN.matcher(fieldName);
        if (!matcher.find()) {
            messager.printMessage(Diagnostic.Kind.ERROR, "Constant names is not comply with a naming convention", field);
        }
    }
}

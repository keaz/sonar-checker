package com.kzone.springchecker.validator.generic;

import com.google.auto.service.AutoService;
import com.kzone.springchecker.validator.BaseValidator;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.*;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.tools.Diagnostic;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.kzone.springchecker.validator.generic.FieldValidator.CONSTANT_FILTER;
import static com.kzone.springchecker.validator.generic.FieldValidator.FIELD_FILTER;
import static com.sun.source.util.TaskEvent.Kind.ANALYZE;
import static com.sun.source.util.TaskEvent.Kind.PARSE;

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
        
        Set<? extends Element> methods = enclosedElements.stream().filter(MethodValidator.METHOD_FILTER).collect(Collectors.toSet());
        System.out.println("*********  Methods "+ methods);
        methods.forEach(method -> methodValidator.validateMethodBody(method));

        List<? extends AnnotationMirror> annotationMirrors = element.getAnnotationMirrors();

    }

}

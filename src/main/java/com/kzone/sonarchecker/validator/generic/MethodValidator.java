package com.kzone.sonarchecker.validator.generic;

import com.google.auto.service.AutoService;
import com.kzone.sonarchecker.validator.BaseValidator;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.*;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.tree.JCTree;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.tools.Diagnostic;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.sun.source.util.TaskEvent.Kind.ANALYZE;

@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class MethodValidator extends BaseValidator {


    private static final Pattern METHOD_NAME_PATTERN = Pattern.compile("^[a-z][a-zA-Z0-9]*$");

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        JavacTask.instance(processingEnv).addTaskListener(new TaskListener() {
            @Override
            public void started(TaskEvent taskEvent) {
                // Don't do anything when event starts
            }

            @Override
            public void finished(TaskEvent taskEvent) {
                if (taskEvent.getKind() == ANALYZE) {
                    new MethodTreeScanner(trees).scan(taskEvent.getCompilationUnit(), null);
                }
            }
        });
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        return false;
    }


    class MethodTreeScanner extends TreePathScanner<Void, Void> {
        private final Trees trees;

        public MethodTreeScanner(Trees trees) {
            this.trees = trees;
        }

        @Override
        public Void visitMethod(MethodTree node, Void unused) {
            super.visitMethod(node, unused);
            TreePath currentPath = getCurrentPath();
            Element method = trees.getElement(trees.getPath(currentPath.getCompilationUnit(), node));

            // filtering out only methods. MethodTree can be a CONSTRUCTOR
            if (method.getKind() == ElementKind.METHOD) {
                validateMethodName(method);
                validateTransactional(method);
            }

            return unused;
        }

        /**
         * //java:S100
         *
         * @param method
         */
        private void validateMethodName(Element method) {
            Matcher matcher = METHOD_NAME_PATTERN.matcher(method.getSimpleName());
            if (!matcher.find()) {
                messager.printMessage(Diagnostic.Kind.ERROR, "Method names should comply with a naming convention", method);
            }
        }

        /**
         * java:S2230
         *
         * @param method
         */
        private void validateTransactional(Element method) {
            Optional<DeclaredType> transactional = method.getAnnotationMirrors().stream()
                    .map(AnnotationMirror::getAnnotationType).filter(declaredType ->
                            declaredType.toString().equals("org.springframework.transaction.annotation.Transactional")).findFirst();

            if (transactional.isPresent() && method.getModifiers().contains(Modifier.PRIVATE)) {
                messager.printMessage(Diagnostic.Kind.ERROR, "Non-public methods should not be \"@Transactional\"", method);
            }
        }




    }
}

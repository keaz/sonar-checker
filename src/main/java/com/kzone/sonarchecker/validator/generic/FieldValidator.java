package com.kzone.sonarchecker.validator.generic;

import com.google.auto.service.AutoService;
import com.kzone.sonarchecker.validator.BaseValidator;
import com.sun.source.tree.*;
import com.sun.source.util.*;
import com.sun.tools.javac.tree.JCTree;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.sun.source.util.TaskEvent.Kind.ANALYZE;

@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class FieldValidator extends BaseValidator {

    private static final Pattern INSTANCE_FIELD_NAME_PATTERN = Pattern.compile("^[a-z][a-zA-Z0-9]*$");
    private static final Pattern CONSTANT_NAME_PATTERN = Pattern.compile("^[A-Z][A-Z0-9]*(_[A-Z0-9]+)*$");

    public static final Predicate<? super Element> FIELD_FILTER = element -> element.getKind() == ElementKind.FIELD && !element.getModifiers().contains(Modifier.STATIC);
    public static final Predicate<? super Element> CONSTANT_FILTER = element -> {
        Set<Modifier> modifiers = element.getModifiers();
        return element.getKind() == ElementKind.FIELD && modifiers.contains(Modifier.STATIC) && modifiers.contains(Modifier.FINAL);
    };

    private Trees trees = null;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        trees = Trees.instance(processingEnv);
        JavacTask.instance(processingEnv).addTaskListener(new TaskListener() {
            @Override
            public void started(TaskEvent taskEvent) {
                // Don't do anything when event starts
            }

            @Override
            public void finished(TaskEvent taskEvent) {
                if (taskEvent.getKind() == ANALYZE) {
                    new FieldTreeScanner(trees).scan(taskEvent.getCompilationUnit(), null);
                }
            }
        });
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        return false;
    }


    class FieldTreeScanner extends TreePathScanner<Void, Void> {
        private final Trees trees;

        public FieldTreeScanner(Trees trees) {
            this.trees = trees;
        }

        @Override
        public Void visitVariable(VariableTree tree, Void aVoid) {
            super.visitVariable(tree, aVoid);
            // This method might be invoked in case of
            //  1. method field definition
            //  2. method parameter
            //  3. local variable declaration
            // Therefore you have to filter out somehow what you don't need.

            if (tree.getKind() != Tree.Kind.VARIABLE) {
                return aVoid;
            }

            TreePath currentPath = getCurrentPath();
            Element variable = trees.getElement(trees.getPath(currentPath.getCompilationUnit(), tree));

            Set<Modifier> modifiers = variable.getModifiers();
            ElementKind kind = variable.getKind();
            if (isField(kind, modifiers)) {
                validateFieldName(variable);
            }else if (isParameter(kind)) {
                validateFieldName(variable);
            }else if (isConstant(kind, modifiers)) {
                validateConstantName(variable);
            }

            // TODO Below implementation is not complete. Tying to determined usage of variable. java:S1481
            TreePath path = trees.getPath(variable);
            Iterator<Tree> iterator = path.iterator();

            while (iterator.hasNext()) {
                Tree next = iterator.next();

                if (next.getKind() == Tree.Kind.BLOCK) {
                    BlockTree block = (BlockTree) next;
                    List<? extends StatementTree> statements = block.getStatements();
                    long count = statements.stream().filter(statement -> statement.toString().contains(variable.toString())).count();
                    if (count == 1) {
                        messager.printMessage(Diagnostic.Kind.ERROR, "Unused variable", variable);
                    }
                } else if (next.getKind() == Tree.Kind.CLASS && variable.getModifiers().contains(Modifier.PRIVATE)) {

                    ClassTree classTreeTree = (ClassTree) next;
                    List<? extends Tree> members = classTreeTree.getMembers();
                    List<? extends JCTree.JCMethodDecl> methods = (List<? extends JCTree.JCMethodDecl>) members
                            .stream().filter(member -> member.getClass() == JCTree.JCMethodDecl.class).collect(Collectors.toList());
                    long count = methods.stream().filter(meth -> meth.body.stats.stream().anyMatch(stat ->
                            stat.toString().contains(variable.toString())
                    )).count();
                    if (count == 0) {
                        messager.printMessage(Diagnostic.Kind.ERROR, "Unused variable", variable);
                    }
                }
            }
            return aVoid;
        }

        private boolean isField(ElementKind kind, Collection<Modifier> modifiers) {
            return kind == ElementKind.FIELD && !modifiers.contains(Modifier.STATIC);
        }

        private boolean isConstant(ElementKind kind, Collection<Modifier> modifiers) {
            return kind == ElementKind.FIELD && modifiers.contains(Modifier.STATIC) && modifiers.contains(Modifier.FINAL);
        }

        private boolean isParameter(ElementKind kind) {
            return kind == ElementKind.PARAMETER;
        }

        private void validateFieldName(Element field) {
            String fieldName = field.getSimpleName().toString();
            Matcher matcher = INSTANCE_FIELD_NAME_PATTERN.matcher(fieldName);
            if (!matcher.find()) {
                messager.printMessage(Diagnostic.Kind.ERROR, "Field names is not comply with a naming convention", field);
            }
        }

        private void validateConstantName(Element field) {
            String fieldName = field.getSimpleName().toString();
            Matcher matcher = CONSTANT_NAME_PATTERN.matcher(fieldName);
            if (!matcher.find()) {
                messager.printMessage(Diagnostic.Kind.ERROR, "Constant names is not comply with a naming convention", field);
            }
        }

    }

}

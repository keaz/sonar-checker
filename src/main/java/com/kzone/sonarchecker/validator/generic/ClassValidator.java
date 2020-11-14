package com.kzone.sonarchecker.validator.generic;

import com.google.auto.service.AutoService;
import com.kzone.sonarchecker.validator.BaseValidator;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.*;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.sun.source.util.TaskEvent.Kind.ANALYZE;

@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class ClassValidator extends BaseValidator {



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
                    new ClassTreeScanner(trees).scan(taskEvent.getCompilationUnit(), null);
                }
            }
        });
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        return false;
    }


    class ClassTreeScanner extends TreePathScanner<Void, Void> {
        private final Trees trees;

        public ClassTreeScanner(Trees trees) {
            this.trees = trees;
        }

        @Override
        public Void visitClass(ClassTree node, Void unused) {
            super.visitClass(node, unused);

            TreePath currentPath = getCurrentPath();
            Element classEl = trees.getElement(trees.getPath(currentPath.getCompilationUnit(), node));

            List<? extends Tree> members = node.getMembers();
            List<? extends MethodTree> methods = (List<? extends MethodTree>) members.stream().filter(member -> member.getKind() == Tree.Kind.METHOD).collect(Collectors.toList());
            List<? extends VariableTree> fields = (List<? extends VariableTree>)members.stream().filter(member -> member.getKind() == Tree.Kind.VARIABLE).collect(Collectors.toList());

            validateEqualsHashCodeMethods(methods);
            checkMethodNameFieldNameSame(methods,fields);
            return unused;
        }

        /**
         * java:S1206
         * @param methods
         */
        private void validateEqualsHashCodeMethods(List<? extends MethodTree> methods) {

            Optional<? extends MethodTree> equalsMethod = methods.stream().filter(method -> {
                List<? extends VariableTree> parameters = method.getParameters();
                if (!method.getName().contentEquals("equals") || parameters.isEmpty()) {
                    return false;
                }
                return parameters.size() == 1 && parameters.get(0).getType().toString().equals("Object");
            }).findAny();

            Optional<? extends MethodTree> hashCodeMethod = methods.stream().filter(method -> {
                List<? extends VariableTree> parameters = method.getParameters();
                return method.getName().contentEquals("hashCode") && parameters.isEmpty();
            }).findAny();

            equalsMethod.ifPresent(methodTree -> {
                if (!hashCodeMethod.isPresent()) {
                    Element method = trees.getElement(trees.getPath(getCurrentPath().getCompilationUnit(), methodTree));
                    messager.printMessage(Diagnostic.Kind.ERROR, "equals(Object obj)\" and \"hashCode()\" should be overridden in pairs", method);
                }
            });

            hashCodeMethod.ifPresent(methodTree -> {
                if (!equalsMethod.isPresent()) {
                    Element method = trees.getElement(trees.getPath(getCurrentPath().getCompilationUnit(), methodTree));
                    messager.printMessage(Diagnostic.Kind.ERROR, "equals(Object obj)\" and \"hashCode()\" should be overridden in pairs", method);
                }
            });
        }

        /**
         * java:S1845
         * @param fields
         * @param methods
         */
        private void checkMethodNameFieldNameSame(List<? extends MethodTree> methods,List<? extends VariableTree> fields){
            List<String> publicFields = fields.stream().filter(field -> field.getModifiers().getFlags().contains(Modifier.PUBLIC))
                    .map(variableTree -> variableTree.getName().toString()).collect(Collectors.toList());
            List<? extends MethodTree> publicMethods = methods.stream().filter(method -> method.getModifiers()
                    .getFlags().contains(Modifier.PUBLIC)).collect(Collectors.toList());
            List<? extends MethodTree> filteredMethods = publicMethods.stream().filter(methodTree ->
                    publicFields.contains(methodTree.getName().toString())).collect(Collectors.toList());

            filteredMethods.forEach(methodTree -> {
                Element method = trees.getElement(trees.getPath(getCurrentPath().getCompilationUnit(), methodTree));
                messager.printMessage(Diagnostic.Kind.ERROR,"Methods and field names should not be the same or differ only by capitalization",method);
            });

        }

        /**
         * java:S1610
         * @param node
         */
        private void validateAbstractClasses(ClassTree node,List<? extends MethodTree> methods,List<? extends VariableTree> fields ){
            boolean isAbstract = node.getModifiers().getFlags().contains(Modifier.ABSTRACT);
            methods.stream().filter(methodTree -> methodTree.getModifiers().getFlags().contains(Modifier.ABSTRACT));
            if(isAbstract && fields.isEmpty()){
                // not going to implement
            }
        }



    }
}

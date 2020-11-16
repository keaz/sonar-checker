package com.kzone.sonarchecker.validator;

import com.sun.source.util.Trees;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;

public abstract class BaseValidator extends AbstractProcessor {

    protected Messager messager ;
    protected Trees trees = null;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        messager = processingEnv.getMessager();
        trees = Trees.instance(processingEnv);
    }
}

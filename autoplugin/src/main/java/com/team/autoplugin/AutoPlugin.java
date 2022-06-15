package com.team.autoplugin;

import org.codehaus.groovy.transform.GroovyASTTransformationClass;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@GroovyASTTransformationClass(classes = AutoPluginGroovyAstTransformation.class)
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.TYPE})
public @interface AutoPlugin {
    String id() default "";

    Class<?> value();
}

package com.team.autoplugin.comm;

import com.google.common.base.Optional;

import java.lang.annotation.Annotation;
import java.util.Iterator;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.SimpleElementVisitor6;

public class ElementsUtil {
    private static final ElementVisitor<PackageElement, Void> PACKAGE_ELEMENT_VISITOR = new SimpleElementVisitor6<PackageElement, Void>() {
        protected PackageElement defaultAction(Element e, Void p) {
            throw new IllegalArgumentException();
        }

        public PackageElement visitPackage(PackageElement e, Void p) {
            return e;
        }
    };
    private static final ElementVisitor<TypeElement, Void> TYPE_ELEMENT_VISITOR = new SimpleElementVisitor6<TypeElement, Void>() {
        protected TypeElement defaultAction(Element e, Void p) {
            throw new IllegalArgumentException();
        }

        public TypeElement visitType(TypeElement e, Void p) {
            return e;
        }
    };
    private static final ElementVisitor<VariableElement, Void> VARIABLE_ELEMENT_VISITOR = new SimpleElementVisitor6<VariableElement, Void>() {
        protected VariableElement defaultAction(Element e, Void p) {
            throw new IllegalArgumentException();
        }

        public VariableElement visitVariable(VariableElement e, Void p) {
            return e;
        }
    };
    private static final ElementVisitor<ExecutableElement, Void> EXECUTABLE_ELEMENT_VISITOR = new SimpleElementVisitor6<ExecutableElement, Void>() {
        protected ExecutableElement defaultAction(Element e, Void p) {
            throw new IllegalArgumentException();
        }

        public ExecutableElement visitExecutable(ExecutableElement e, Void p) {
            return e;
        }
    };

    public static PackageElement getPackage(Element element) {
        while (element.getKind() != ElementKind.PACKAGE) {
            element = element.getEnclosingElement();
        }

        return (PackageElement) element;
    }

   /* public static PackageElement asPackage(Element element) {
        return (PackageElement) element.accept(PACKAGE_ELEMENT_VISITOR, (Object) null);
    }*/

    public static TypeElement asType(Element element) {
        return (TypeElement) element.accept(TYPE_ELEMENT_VISITOR, null);
    }

    /*public static VariableElement asVariable(Element element) {
        return (VariableElement) element.accept(VARIABLE_ELEMENT_VISITOR, (Object) null);
    }

    public static ExecutableElement asExecutable(Element element) {
        return (ExecutableElement) element.accept(EXECUTABLE_ELEMENT_VISITOR, (Object) null);
    }
*/
    public static boolean isAnnotationPresent(Element element, Class<? extends Annotation> annotationClass) {
        return getAnnotationMirror(element, annotationClass).isPresent();
    }

    public static Optional<AnnotationMirror> getAnnotationMirror(Element element, Class<? extends Annotation> annotationClass) {
        String annotationClassName = annotationClass.getCanonicalName();
        Iterator i$ = element.getAnnotationMirrors().iterator();

        AnnotationMirror annotationMirror;
        TypeElement annotationTypeElement;
        do {
            if (!i$.hasNext()) {
                return Optional.absent();
            }

            annotationMirror = (AnnotationMirror) i$.next();
            annotationTypeElement = asType(annotationMirror.getAnnotationType().asElement());
        } while (!annotationTypeElement.getQualifiedName().contentEquals(annotationClassName));

        return Optional.of(annotationMirror);
    }

    private ElementsUtil() {
    }
}

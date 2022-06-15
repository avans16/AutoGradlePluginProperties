package com.team.autoplugin;

import com.google.auto.service.AutoService;
import com.team.autoplugin.comm.ElementsUtil;
import com.team.autoplugin.comm.PropertiesPath;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedOptions;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

@AutoService(Processor.class)
@SupportedOptions({"debug", "check"})
public class AutoPluginProcessor extends AbstractProcessor {
    private final Hashtable<String, String> plugins = new Hashtable<>();

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton(AutoPlugin.class.getName());
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        System.out.println("start process auto plugin");
        try {
            if (roundEnvironment.processingOver()) {
                outputImplementClassFile();
            } else {
                processAnnotations(set, roundEnvironment);
            }
        } catch (Exception e) {
            StringWriter writer = new StringWriter();
            e.printStackTrace(new PrintWriter(writer));
            fatalError(writer.toString());
        }
        return true;
    }

    private void processAnnotations(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(AutoPlugin.class);
        this.log(set.toString());
        this.log(elements.toString());
        for (Element element : elements) {
            TypeElement providerInterfaceImpl = (TypeElement) element;
            AnnotationMirror mirror = ElementsUtil.getAnnotationMirror(providerInterfaceImpl, AutoPlugin.class).get();
            String id = null;
            TypeElement providerInterface = null;
            Map<? extends ExecutableElement, ? extends AnnotationValue> values = mirror.getElementValues();
            for (ExecutableElement executableElement : values.keySet()) {
                Name methodName = executableElement.getSimpleName();
                Object returnValue = values.get(executableElement).getValue();
                if ("id".equals(methodName.toString())) {
                    id = (String) returnValue;
                } else if ("value".equals(methodName.toString())) {
                    providerInterface = (TypeElement) ((DeclaredType) returnValue).asElement();
                }
            }
            String implName;
            if (!this.checkImplementer(providerInterfaceImpl, providerInterface)) {
                implName = "plugin must implement their provider interface. " + providerInterfaceImpl.getQualifiedName() + " does not implement " + providerInterface.getQualifiedName();
                this.error(implName, providerInterfaceImpl, mirror);
            }
            implName = getBinaryNameImpl(providerInterfaceImpl, providerInterfaceImpl.getSimpleName().toString());
            id = id == null || "".equals(id) ? implName : id;
            if (plugins.containsKey(id)) {
                this.fatalError("the plugin id " + id + " has already exist,try to replace a new id");
            } else {
                plugins.put(id, implName);
            }
        }
    }

    private void outputImplementClassFile() {
        Filer filer = this.processingEnv.getFiler();

        for (String pluginId : this.plugins.keySet()) {
            Properties properties = new Properties();
            String pluginFile = PropertiesPath.parsePath(pluginId);
            this.log("pluginId=" + pluginId);
            this.log("pluginFile=" + pluginFile);
            try {
                try {
                    FileObject existFile = filer.getResource(StandardLocation.CLASS_OUTPUT, "", pluginFile);
                    this.log("looking for existing plugin file at " + existFile.toUri());
                    properties.load(existFile.openInputStream());
                } catch (IOException e) {
                    this.log("plugin file did not already exist.");
                }
                properties.setProperty("implementation-class", this.plugins.get(pluginId));
                FileObject fileObject = filer.createResource(StandardLocation.CLASS_OUTPUT, "", pluginFile);
                OutputStream out = fileObject.openOutputStream();
                properties.store(out, "The gradle plugin " + pluginId + " implement class");
                out.close();
                this.log("Wrote to: " + fileObject.toUri());
            } catch (IOException e) {
                this.fatalError("Unable to create " + pluginFile + ", " + e);
                return;
            }
        }

    }

    private void log(String msg) {
        if (this.processingEnv.getOptions().containsKey("debug")) {
            this.processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, msg);
        }
    }

    private void error(String msg, Element element, AnnotationMirror annotation) {
        this.processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, msg, element, annotation);
    }

    private void fatalError(String msg) {
        this.processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "FATAL ERROR: " + msg);
    }

    private boolean checkImplementer(TypeElement providerImplementer, TypeElement providerType) {
        String verify = this.processingEnv.getOptions().get("check");
        if (verify != null && Boolean.valueOf(verify)) {
            Types types = this.processingEnv.getTypeUtils();
            return types.isSubtype(providerImplementer.asType(), providerType.asType());
        } else {
            return true;
        }
    }

    private String getBinaryNameImpl(TypeElement element, String className) {
        Element enclosingElement = element.getEnclosingElement();
        if (enclosingElement instanceof PackageElement) {
            PackageElement pkg = (PackageElement) enclosingElement;
            return pkg.isUnnamed() ? className : pkg.getQualifiedName() + "." + className;
        } else {
            TypeElement typeElement = (TypeElement) enclosingElement;
            return this.getBinaryNameImpl(typeElement, typeElement.getSimpleName() + "$" + className);
        }
    }
}
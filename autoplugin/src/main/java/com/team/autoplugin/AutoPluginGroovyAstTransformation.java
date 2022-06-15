package com.team.autoplugin;

import com.team.autoplugin.comm.PropertiesPath;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.transform.ASTTransformation;
import org.codehaus.groovy.transform.GroovyASTTransformation;

import java.io.File;
import java.io.OutputStream;
import java.util.Properties;

import javax.tools.FileObject;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;


@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
public class AutoPluginGroovyAstTransformation implements ASTTransformation {
    private boolean check = true;

    @Override
    public void visit(ASTNode[] astNodes, SourceUnit sourceUnit) {
        log("========================================");
        log("start generate plugin properties");
        //annotation
        AnnotationNode annotationNode = (AnnotationNode) astNodes[0];
        //annotation class
        ClassNode classNode = (ClassNode) astNodes[1];
        try {
            Expression constantExpression = annotationNode.getMember("id");
            Expression classExpression = annotationNode.getMember("value");

            String id = null;
            if (constantExpression != null) {
                id = (String) ((ConstantExpression) constantExpression).getValue();
            }
            ClassNode interfaceOri = null;
            if (classExpression != null) {
                interfaceOri = classExpression.getType();
            }
            if (interfaceOri == null) {
                fatalError("interface must not null,set annotation param; value=xxx.class");
            } else if (check) {
                //check subClass
                log("interface is:" + interfaceOri.getName());
                log("implement is:" + classNode.getName());
                if (interfaceOri.isInterface()) {
                    //interface
                    if (!classNode.implementsInterface(interfaceOri)) {
                        fatalError(classNode.getName() + " must implement " + interfaceOri.getName());
                    }
                } else {
                    //class
                    Class<?> oriClass = interfaceOri.getTypeClass();
                    Class<?> implClass = classNode.getSuperClass().getTypeClass();
                    if (!oriClass.isAssignableFrom(implClass)) {
                        fatalError(classNode.getName() + " must implement " + interfaceOri.getName());
                    }
                }
            }
            if (id == null || id.isEmpty()) {
                id = classNode.getName();
                log("plugin id is null,use default id " + id);
            }
            outputImplementClassFile(sourceUnit, id, classNode.getName());
        } catch (Exception e) {
            error(e);
        }
    }

    private void outputImplementClassFile(SourceUnit sourceUnit, String pluginId, String implementClass) {
        try {
            log("on create properties by plugin id " + pluginId);
            String pluginFile = PropertiesPath.parsePath(pluginId);
            File outputClassDir = sourceUnit.getConfiguration().getTargetDirectory();
            File targetFileDir = new File(outputClassDir, pluginFile);
            log("classOutput dir:" + outputClassDir.getPath());
            log("properties output dir:" + targetFileDir.getPath());
            Properties properties = new Properties();
            properties.setProperty("implementation-class", implementClass);
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
            Iterable<? extends JavaFileObject> fileObjects = fileManager.getJavaFileObjects(targetFileDir);
            for (FileObject fileObject : fileObjects) {
                OutputStream out = fileObject.openOutputStream();
                properties.store(out, "The gradle plugin " + pluginId + " implement class");
                out.close();
            }
            log("success !!!!");
            log("========================================");
        } catch (Exception e) {
            error(e);
        }
    }

    private RuntimeException fatalError(String msg) {
        throw new RuntimeException("fatalError:" + msg);
    }

    private void log(String msg) {
        System.out.println(msg);
    }

    private void error(Exception e) {
        if (e != null) {
            e.printStackTrace(System.out);
        }
    }
}

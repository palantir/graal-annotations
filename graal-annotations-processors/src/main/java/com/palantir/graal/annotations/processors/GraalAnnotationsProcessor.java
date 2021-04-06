/*
 * (c) Copyright 2018 Palantir Technologies Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.palantir.graal.annotations.processors;

import com.google.auto.service.AutoService;
import com.google.common.collect.Lists;
import com.palantir.graal.annotations.GraalReflectable;
import com.palantir.graal.reflection.FieldReflectionConfig;
import com.palantir.graal.reflection.MethodReflectionConfig;
import com.palantir.graal.reflection.ReflectionConfigFile;
import com.palantir.graal.reflection.SummaryClassReflectionConfig;
import java.io.IOException;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

@AutoService(Processor.class)
@SupportedAnnotationTypes("com.palantir.graal.annotations.GraalReflectable")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public final class GraalAnnotationsProcessor extends AbstractProcessor {

    private ReflectionConfigFile reflection = new ReflectionConfigFile();

    @Override
    public boolean process(Set<? extends TypeElement> _annotations, RoundEnvironment roundEnv) {
        try {
            if (roundEnv.processingOver()) {
                FileObject fileObject = processingEnv.getFiler()
                        .createResource(StandardLocation.CLASS_OUTPUT, "", ReflectionConfigFile.PATH);
                reflection.render(fileObject);
            } else {
                processImpl(roundEnv);
            }
        } catch (IOException | RuntimeException e) {
            // do not allow exceptions to reach the compiler
            error(e.getMessage(), null);
        }
        return true;
    }

    private void processImpl(RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(GraalReflectable.class)) {

            // grab config from annotation
            GraalReflectable config = getExactlyOneGraalReflectableAnnotationOrNull(element);
            if (config == null) {
                continue;
            }

            switch (element.getKind()) {
                case FIELD:
                    processField((VariableElement) element, config);
                    break;
                case CONSTRUCTOR:
                case METHOD:
                    processMethod((ExecutableElement) element);
                    break;
                case CLASS:
                    processClass((TypeElement) element, config);
                    break;
                default:
                    error("@GraalReflectable annotation appears on unexpected element kind "
                            + element.getKind(), element);
                    break;
            }
        }
    }

    private void processField(VariableElement element, GraalReflectable config) {
        TypeElement classElement = (TypeElement) element.getEnclosingElement();

        reflection.addField(classElement.getQualifiedName().toString(), FieldReflectionConfig.builder()
                .name(element.getSimpleName().toString())
                .allowWrite(config.allowWrite())
                .build());
    }

    private void processMethod(ExecutableElement element) {
        TypeElement classElement = (TypeElement) element.getEnclosingElement();

        reflection.addMethod(classElement.getQualifiedName().toString(), MethodReflectionConfig.builder()
                .name(element.getSimpleName().toString())
                .parameterTypes(Lists.transform(element.getParameters(), ve -> ve.asType().toString()))
                .build());
    }

    private void processClass(TypeElement element, GraalReflectable config) {
        reflection.addClass(SummaryClassReflectionConfig.builder()
                .name(element.getQualifiedName().toString())
                .allDeclaredConstructors(config.allDeclaredConstructors())
                .allDeclaredFields(config.allDeclaredFields())
                .allDeclaredMethods(config.allDeclaredMethods())
                .allPublicConstructors(config.allPublicConstructors())
                .allPublicFields(config.allPublicFields())
                .allPublicMethods(config.allPublicMethods())
                .build());
    }

    private GraalReflectable getExactlyOneGraalReflectableAnnotationOrNull(Element element) {
        GraalReflectable[] config = element.getAnnotationsByType(GraalReflectable.class);
        if (config.length != 1) {
            error("Expected to find exactly one @GraalReflectable, but found " + config.length, element);
            return null;
        }
        return config[0];
    }

    private void error(String message, Element element) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, message, element);
    }
}

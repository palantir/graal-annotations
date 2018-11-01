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

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.Compiler.javac;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import java.io.IOException;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import org.junit.Test;

public final class GraalAnnotationsProccessorTests {
    @Test
    public void testNoOpWithNoAnnotations() {
        Compilation compilation = javac().withProcessors(new GraalAnnotationsProcessor())
                .compile(JavaFileObjects.forSourceString("Test",
                        "final class Test {}\n"));

        assertThat(compilation).succeededWithoutWarnings();
    }

    @Test
    public void testClassAnnotation() throws IOException {
        Compilation compilation = javac().withProcessors(new GraalAnnotationsProcessor())
                .compile(JavaFileObjects.forSourceString("Test",
                        "import com.palantir.graal.annotations.GraalReflectable;\n"
                                + "\n"
                                + "@GraalReflectable\n"
                                + "final class Test {}\n"));

        assertThat(compilation)
                .generatedFile(StandardLocation.CLASS_OUTPUT, "META-INF/graal/reflection-config.json")
                .contentsAsUtf8String()
                .isEqualTo("[ {\n"
                        + "  \"name\" : \"Test\",\n"
                        + "  \"allPublicConstructors\" : true,\n"
                        + "  \"allPublicMethods\" : true,\n"
                        + "  \"allPublicFields\" : true,\n"
                        + "  \"allDeclaredConstructors\" : false,\n"
                        + "  \"allDeclaredMethods\" : true,\n"
                        + "  \"allDeclaredFields\" : true\n"
                        + "} ]");
    }

    @Test
    public void testClassAnnotationWithSettings() throws IOException {
        Compilation compilation = javac().withProcessors(new GraalAnnotationsProcessor())
                .compile(JavaFileObjects.forSourceString("Test",
                        "import com.palantir.graal.annotations.GraalReflectable;\n"
                                + "\n"
                                + "@GraalReflectable(\n"
                                + "  allPublicConstructors = false,\n"
                                + "  allPublicMethods = false,\n"
                                + "  allPublicFields = false,\n"
                                + "  allDeclaredConstructors = true,\n"
                                + "  allDeclaredMethods = true,\n"
                                + "  allDeclaredFields = true)\n"
                                + "final class Test {}\n"));

        assertThat(compilation)
                .generatedFile(StandardLocation.CLASS_OUTPUT, "META-INF/graal/reflection-config.json")
                .contentsAsUtf8String()
                .isEqualTo("[ {\n"
                        + "  \"name\" : \"Test\",\n"
                        + "  \"allPublicConstructors\" : false,\n"
                        + "  \"allPublicMethods\" : false,\n"
                        + "  \"allPublicFields\" : false,\n"
                        + "  \"allDeclaredConstructors\" : true,\n"
                        + "  \"allDeclaredMethods\" : true,\n"
                        + "  \"allDeclaredFields\" : true\n"
                        + "} ]");
    }

    @Test
    public void testConstructorAnnotations() throws IOException {
        Compilation compilation = javac().withProcessors(new GraalAnnotationsProcessor())
                .compile(JavaFileObjects.forSourceString("Test",
                        "import com.palantir.graal.annotations.GraalReflectable;\n"
                                + "import java.util.*;\n"
                                + "\n"
                                + "final class Test {\n"
                                + "  @GraalReflectable\n"
                                + "  public Test() {}\n"
                                + "  @GraalReflectable\n"
                                + "  public Test(int foo, List<String> bar) {}\n"
                                + "}\n"));

        assertThat(compilation)
                .generatedFile(StandardLocation.CLASS_OUTPUT, "META-INF/graal/reflection-config.json")
                .contentsAsUtf8String()
                .isEqualTo("[ {\n"
                        + "  \"name\" : \"Test\",\n"
                        + "  \"fields\" : [ ],\n"
                        + "  \"methods\" : [ {\n"
                        + "    \"name\" : \"<init>\",\n"
                        + "    \"parameterTypes\" : [ ]\n"
                        + "  }, {\n"
                        + "    \"name\" : \"<init>\",\n"
                        + "    \"parameterTypes\" : [ \"int\", \"java.util.List<java.lang.String>\" ]\n"
                        + "  } ]\n"
                        + "} ]");
    }

    @Test
    public void testMethodAnnotations() throws IOException {
        Compilation compilation = javac().withProcessors(new GraalAnnotationsProcessor())
                .compile(JavaFileObjects.forSourceString("Test",
                        "import com.palantir.graal.annotations.GraalReflectable;\n"
                                + "import java.util.*;\n"
                                + "\n"
                                + "final class Test {\n"
                                + "  @GraalReflectable\n"
                                + "  public void test() {}\n"
                                + "  @GraalReflectable\n"
                                + "  public void testParameters(int foo, List<String> bar) {}\n"
                                + "}\n"));

        assertThat(compilation)
                .generatedFile(StandardLocation.CLASS_OUTPUT, "META-INF/graal/reflection-config.json")
                .contentsAsUtf8String()
                .isEqualTo("[ {\n"
                        + "  \"name\" : \"Test\",\n"
                        + "  \"fields\" : [ ],\n"
                        + "  \"methods\" : [ {\n"
                        + "    \"name\" : \"test\",\n"
                        + "    \"parameterTypes\" : [ ]\n"
                        + "  }, {\n"
                        + "    \"name\" : \"testParameters\",\n"
                        + "    \"parameterTypes\" : [ \"int\", \"java.util.List<java.lang.String>\" ]\n"
                        + "  } ]\n"
                        + "} ]");
    }

    @Test
    public void testFieldAnnotations() throws IOException {
        Compilation compilation = javac().withProcessors(new GraalAnnotationsProcessor())
                .compile(JavaFileObjects.forSourceString("Test",
                        "import com.palantir.graal.annotations.GraalReflectable;\n"
                                + "import java.util.*;\n"
                                + "\n"
                                + "final class Test {\n"
                                + "  @GraalReflectable List<String> foo;\n"
                                + "  @GraalReflectable int bar;\n"
                                + "}\n"));

        assertThat(compilation)
                .generatedFile(StandardLocation.CLASS_OUTPUT, "META-INF/graal/reflection-config.json")
                .contentsAsUtf8String()
                .isEqualTo("[ {\n"
                        + "  \"name\" : \"Test\",\n"
                        + "  \"fields\" : [ {\n"
                        + "    \"name\" : \"foo\",\n"
                        + "    \"allowWrite\" : false\n"
                        + "  }, {\n"
                        + "    \"name\" : \"bar\",\n"
                        + "    \"allowWrite\" : false\n"
                        + "  } ],\n"
                        + "  \"methods\" : [ ]\n"
                        + "} ]");
    }

    @Test
    public void testFieldAnnotationsWithSettings() throws IOException {
        Compilation compilation = javac().withProcessors(new GraalAnnotationsProcessor())
                .compile(JavaFileObjects.forSourceString("Test",
                        "import com.palantir.graal.annotations.GraalReflectable;\n"
                                + "import java.util.*;\n"
                                + "\n"
                                + "final class Test {\n"
                                + "  @GraalReflectable(allowWrite = true) List<String> foo;\n"
                                + "  @GraalReflectable int bar;\n"
                                + "}\n"));

        assertThat(compilation)
                .generatedFile(StandardLocation.CLASS_OUTPUT, "META-INF/graal/reflection-config.json")
                .contentsAsUtf8String()
                .isEqualTo("[ {\n"
                        + "  \"name\" : \"Test\",\n"
                        + "  \"fields\" : [ {\n"
                        + "    \"name\" : \"foo\",\n"
                        + "    \"allowWrite\" : true\n"
                        + "  }, {\n"
                        + "    \"name\" : \"bar\",\n"
                        + "    \"allowWrite\" : false\n"
                        + "  } ],\n"
                        + "  \"methods\" : [ ]\n"
                        + "} ]");
    }

    @Test
    public void testMixedAnnotations() throws IOException {
        Compilation compilation = javac().withProcessors(new GraalAnnotationsProcessor())
                .compile(JavaFileObjects.forSourceString("Test",
                        "import com.palantir.graal.annotations.GraalReflectable;\n"
                                + "import java.util.*;\n"
                                + "\n"
                                + "@GraalReflectable\n"
                                + "final class Test {\n"
                                + "  @GraalReflectable List<String> foo;\n"
                                + "  @GraalReflectable\n"
                                + "  public void test() {}\n"
                                + "  public void testParameters(int foo, List<String> bar) {}\n"
                                + "}\n"));

        // when both class and method|field annotations are present, keep the most specific settings
        assertThat(compilation)
                .generatedFile(StandardLocation.CLASS_OUTPUT, "META-INF/graal/reflection-config.json")
                .contentsAsUtf8String()
                .isEqualTo("[ {\n"
                        + "  \"name\" : \"Test\",\n"
                        + "  \"fields\" : [ {\n"
                        + "    \"name\" : \"foo\",\n"
                        + "    \"allowWrite\" : false\n"
                        + "  } ],\n"
                        + "  \"methods\" : [ {\n"
                        + "    \"name\" : \"test\",\n"
                        + "    \"parameterTypes\" : [ ]\n"
                        + "  } ]\n"
                        + "} ]");
    }

    @Test
    public void testMultipleSourceFiles() {
        Compilation compilation = javac().withProcessors(new GraalAnnotationsProcessor())
                .compile(JavaFileObjects.forSourceString("TestOne",
                        "import com.palantir.graal.annotations.GraalReflectable;\n"
                                + "import java.util.*;\n"
                                + "\n"
                                + "@GraalReflectable\n"
                                + "final class TestOne {\n"
                                + "  @GraalReflectable List<String> foo;\n"
                                + "  @GraalReflectable\n"
                                + "  public void test() {}\n"
                                + "  public void testParameters(int foo, List<String> bar) {}\n"
                                + "}\n"),
                        JavaFileObjects.forSourceString("TestTwo",
                                "import com.palantir.graal.annotations.GraalReflectable;\n"
                                        + "import java.util.*;\n"
                                        + "\n"
                                        + "@GraalReflectable\n"
                                        + "final class TestTwo {}\n"),
                        JavaFileObjects.forSourceString("TestThree", "final class TestThree {}\n"));

        assertThat(compilation)
                .generatedFile(StandardLocation.CLASS_OUTPUT, "META-INF/graal/reflection-config.json")
                .contentsAsUtf8String()
                .isEqualTo("[ {\n"
                        + "  \"name\" : \"TestTwo\",\n"
                        + "  \"allPublicConstructors\" : true,\n"
                        + "  \"allPublicMethods\" : true,\n"
                        + "  \"allPublicFields\" : true,\n"
                        + "  \"allDeclaredConstructors\" : false,\n"
                        + "  \"allDeclaredMethods\" : true,\n"
                        + "  \"allDeclaredFields\" : true\n"
                        + "}, {\n"
                        + "  \"name\" : \"TestOne\",\n"
                        + "  \"fields\" : [ {\n"
                        + "    \"name\" : \"foo\",\n"
                        + "    \"allowWrite\" : false\n"
                        + "  } ],\n"
                        + "  \"methods\" : [ {\n"
                        + "    \"name\" : \"test\",\n"
                        + "    \"parameterTypes\" : [ ]\n"
                        + "  } ]\n"
                        + "} ]");
    }

    @Test
    public void testErrorForMultipleGraalReflectableAnnotations() throws IOException {
        JavaFileObject sourceFile = JavaFileObjects.forSourceString("Test",
                "import com.palantir.graal.annotations.GraalReflectable;\n"
                        + "\n"
                        + "@GraalReflectable\n"
                        + "@GraalReflectable\n"
                        + "final class Test {\n"
                        + "  @GraalReflectable\n"
                        + "  @GraalReflectable\n"
                        + "  int foo;\n"
                        + "  @GraalReflectable\n"
                        + "  @GraalReflectable\n"
                        + "  public void test() {}\n"
                        + "}\n");

        Compilation compilation = javac().withProcessors(new GraalAnnotationsProcessor()).compile(sourceFile);
        assertThat(compilation)
                .hadErrorContaining(
                        "com.palantir.graal.annotations.GraalReflectable is not a repeatable annotation type")
                .inFile(sourceFile)
                .onLine(4)
                .atColumn(1);
        assertThat(compilation)
                .hadErrorContaining(
                        "com.palantir.graal.annotations.GraalReflectable is not a repeatable annotation type")
                .inFile(sourceFile)
                .onLine(7)
                .atColumn(3);
        assertThat(compilation)
                .hadErrorContaining(
                        "com.palantir.graal.annotations.GraalReflectable is not a repeatable annotation type")
                .inFile(sourceFile)
                .onLine(10)
                .atColumn(3);
    }
}

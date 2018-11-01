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

package com.palantir.graal.reflection;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.base.Preconditions;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.tools.FileObject;

/**
 * Wrapper for managing SubstrateVM's JSON format for reflection configuration, see
 * https://github.com/oracle/graal/blob/master/substratevm/REFLECTION.md#manual-configuration [github.com].
 */
public final class ReflectionConfigFile {

    public static final String PATH = "META-INF/graal/reflection-config.json";

    private final Map<String, ImmutableSpecificClassReflectionConfig.Builder> specifics;
    private final Map<String, SummaryClassReflectionConfig> summaries;

    public ReflectionConfigFile() {
        specifics = new HashMap<>();
        summaries = new HashMap<>();
    }

    public void addField(String className, FieldReflectionConfig config) {
        getOrCreateSpecificBuilder(className).addFields(config);
    }

    public void addMethod(String className, MethodReflectionConfig config) {
        getOrCreateSpecificBuilder(className).addMethods(config);
    }

    public void addClass(SummaryClassReflectionConfig config) {
        summaries.put(config.name(), config);
    }

    public void render(FileObject fileObject) throws IOException {
        List<ReflectionConfigElement> elements = new ArrayList<>();

        summaries.forEach((k, v) -> {
            if (!specifics.containsKey(k)) {
                elements.add(v);
            }
        });

        specifics.forEach((k, v) -> {
            elements.add(v.build());
        });

        File resultFile = new File(PATH);
        File parentDir = resultFile.getParentFile();
        Preconditions.checkState(parentDir.mkdirs() || parentDir.exists(),
                "Unable to create @GraalReflectable output directory '%s' and directory does not exist.",
                parentDir.getAbsolutePath());

        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        try (OutputStream os = fileObject.openOutputStream()) {
            mapper.writeValue(os, elements);
        }
    }

    private ImmutableSpecificClassReflectionConfig.Builder getOrCreateSpecificBuilder(String className) {
        ImmutableSpecificClassReflectionConfig.Builder classConfig = specifics.get(className);
        if (classConfig == null) {
            classConfig = ImmutableSpecificClassReflectionConfig.builder()
                .name(className);
            specifics.put(className, classConfig);
        }
        return classConfig;
    }
}

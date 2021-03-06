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

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

/**
 * Value type corresponding to SubstrateVM's JSON format for reflection configuration, see
 * https://github.com/oracle/graal/blob/master/substratevm/REFLECTION.md#manual-configuration [github.com].
 */
@JsonSerialize(as = ImmutableFieldReflectionConfig.class)
@Value.Immutable
public interface FieldReflectionConfig {
    String name();
    boolean allowWrite();

    static ImmutableFieldReflectionConfig.Builder builder() {
        return ImmutableFieldReflectionConfig.builder();
    }
}

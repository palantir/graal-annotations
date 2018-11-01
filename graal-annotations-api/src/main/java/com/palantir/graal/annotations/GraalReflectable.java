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

package com.palantir.graal.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A marker that designates the labeled element may be used with reflection in SubstrateVM and with configuration
 * options that match those described in https://github.com/oracle/graal/blob/master/substratevm/REFLECTION.md.
 * <p>
 * When applied to a {@link ElementType#TYPE}, use the following options to automatically include an entire set of
 * members of a class:
 * <ul>
 *     <li>{@link #allPublicConstructors()} default true</li>
 *     <li>{@link #allPublicMethods()} default true</li>
 *     <li>{@link #allPublicFields()} default true</li>
 *     <li>{@link #allDeclaredConstructors()} default false</li>
 *     <li>{@link #allDeclaredMethods()} default false</li>
 *     <li>{@link #allDeclaredFields()} default false</li>
 * </ul>
 * <p>
 * When applied to a {@link ElementType#FIELD}, use the following options to change handling of {@code final} fields:
 * <ul>
 *     <li>{@link #allowWrite()} default false: allow writing the field despite its demarcation as final</li>
 * </ul>
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.CONSTRUCTOR, ElementType.METHOD})
public @interface GraalReflectable {
    /** Takes effect only when applied to a {@link ElementType#TYPE}. */
    boolean allPublicConstructors() default true;

    /** Takes effect only when applied to a {@link ElementType#TYPE}. */
    boolean allPublicMethods() default true;

    /** Takes effect only when applied to a {@link ElementType#TYPE}. */
    boolean allPublicFields() default true;

    /** Takes effect only when applied to a {@link ElementType#TYPE}. */
    boolean allDeclaredConstructors() default false;

    /** Takes effect only when applied to a {@link ElementType#TYPE}. */
    boolean allDeclaredMethods() default true;

    /** Takes effect only when applied to a {@link ElementType#TYPE}. */
    boolean allDeclaredFields() default true;

    /** Takes effect only when applied to a {@link ElementType#FIELD}. */
    boolean allowWrite() default false;
}

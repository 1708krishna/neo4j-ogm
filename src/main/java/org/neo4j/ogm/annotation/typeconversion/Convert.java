/*
 * Copyright (c)  [2011-2015] "Neo Technology" / "Graph Aware Ltd."
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with
 * separate copyright notices and license terms. Your use of the source
 * code for these subcomponents is subject to the terms and
 * conditions of the subcomponent's license, as noted in the LICENSE file.
 */

package org.neo4j.ogm.annotation.typeconversion;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.neo4j.ogm.typeconversion.AttributeConverter;

/**
 * Annotation to be applied to fields and accessor methods of entity properties to specify the AttributeConverter to use for
 * writing or reading its value in the graph database.
 *
 * @author Vince Bickers
 * @author Adam George
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
@Inherited
public @interface Convert {

    static final String CLASS = "org.neo4j.ogm.annotation.typeconversion.Convert";
    static final String CONVERTER = "value";

    Class<? extends AttributeConverter<?, ?>> value() default Unset.class;

    /** Placeholder to allow the annotation to be applied without specifying an explicit converter implementation. */
    static abstract class Unset implements AttributeConverter<Object, Object> {}

}


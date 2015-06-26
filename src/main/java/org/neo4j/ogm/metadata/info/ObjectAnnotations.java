/*
 * Copyright (c) 2002-2015 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with
 * separate copyright notices and license terms. Your use of the source
 * code for these subcomponents is subject to the terms and
 * conditions of the subcomponent's license, as noted in the LICENSE file.
 *
 */

package org.neo4j.ogm.metadata.info;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.ogm.annotation.typeconversion.Convert;
import org.neo4j.ogm.annotation.typeconversion.DateLong;
import org.neo4j.ogm.annotation.typeconversion.DateString;
import org.neo4j.ogm.annotation.typeconversion.EnumString;
import org.neo4j.ogm.annotation.typeconversion.NumberString;
import org.neo4j.ogm.typeconversion.AttributeConverter;
import org.neo4j.ogm.typeconversion.DateLongConverter;
import org.neo4j.ogm.typeconversion.DateStringConverter;
import org.neo4j.ogm.typeconversion.EnumStringConverter;
import org.neo4j.ogm.typeconversion.NumberStringConverter;

/**
 * @author Vince Bickers
 */
public class ObjectAnnotations {

    private String objectName; // fully qualified class, method or field name.
    private final Map<String, AnnotationInfo> annotations = new HashMap<>();

    public String getName() {
        return objectName;
    }

    public void setName(String objectName) {
        this.objectName = objectName;
    }

    public void put(String key, AnnotationInfo value) {
        annotations.put(key, value);
    }

    public AnnotationInfo get(String key) {
        return annotations.get(key);
    }

    public boolean isEmpty() {
        return annotations.isEmpty();
    }

    public AttributeConverter<?, ?> getConverter(String typeDescriptor) {

        // try to get a custom type converter
        AnnotationInfo customType = get(Convert.CLASS);
        if (customType != null) {
            try {
                String classDescriptor = customType.get(Convert.CONVERTER, null);
                String className = classDescriptor.replace("/", ".").substring(1, classDescriptor.length()-1);
                Class clazz = Class.forName(className);
                return (AttributeConverter<?, ?>) clazz.newInstance();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        // try to find a pre-registered type annotation. this is very clumsy, but at least it is done only once
        AnnotationInfo dateLongConverterInfo = get(DateLong.CLASS);
        if (dateLongConverterInfo != null) {
            return new DateLongConverter();
        }

        AnnotationInfo dateStringConverterInfo = get(DateString.CLASS);
        if (dateStringConverterInfo != null) {
            String format = dateStringConverterInfo.get(DateString.FORMAT, DateString.ISO_8601);
            return new DateStringConverter(format);
        }

        AnnotationInfo enumStringConverterInfo = get(EnumString.CLASS);
        if (enumStringConverterInfo != null) {
            String classDescriptor = enumStringConverterInfo.get(EnumString.TYPE, null);
            String className = classDescriptor.replace("/", ".").substring(1, classDescriptor.length()-1);
            try {
                Class clazz = Class.forName(className);
                return new EnumStringConverter(clazz);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        AnnotationInfo numberStringConverterInfo = get(NumberString.CLASS);
        if (numberStringConverterInfo != null) {
            String classDescriptor = enumStringConverterInfo.get(NumberString.TYPE, null);
            String className = classDescriptor.replace("/", ".").substring(1, classDescriptor.length()-1);
            try {
                Class clazz = Class.forName(className);
                return new NumberStringConverter(clazz);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return null;
    }

}

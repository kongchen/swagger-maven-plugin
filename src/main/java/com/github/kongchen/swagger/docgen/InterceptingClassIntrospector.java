package com.github.kongchen.swagger.docgen;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.BasicBeanDescription;
import com.fasterxml.jackson.databind.introspect.BasicClassIntrospector;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import io.swagger.annotations.ApiModelProperty;

import java.lang.annotation.Annotation;

public class InterceptingClassIntrospector extends BasicClassIntrospector {

    private boolean preferSwaggerValues;

    public InterceptingClassIntrospector(boolean preferSwaggerValues) {
        this.preferSwaggerValues = preferSwaggerValues;
    }

    @Override
    public BasicBeanDescription forSerialization(SerializationConfig cfg, JavaType type, MixInResolver r) {
        final BasicBeanDescription basicBeanDescription = super.forSerialization(cfg, type, r);

        if(!preferSwaggerValues) {
            redactApiModelPropertyValues(basicBeanDescription);
        }

        return basicBeanDescription;
    }

    private void redactApiModelPropertyValues(BasicBeanDescription basicBeanDescription) {
        for (BeanPropertyDefinition propDef : basicBeanDescription.findProperties()) {
            final AnnotatedMember member = propDef.getPrimaryMember();
            if (member instanceof AnnotatedMember) {
                redactApiModelPropertyValues(member);
            }
        }
    }

    private void redactApiModelPropertyValues(AnnotatedMember member) {
        final ApiModelProperty mp = member.getAnnotation(ApiModelProperty.class);
        if(mp != null) {
            Annotation newAnnotation = new RedactedApiModelProperty(mp);
            member.addOrOverride(newAnnotation);
        }
    }

    private static class RedactedApiModelProperty implements ApiModelProperty {

        private ApiModelProperty mp;

        public RedactedApiModelProperty(ApiModelProperty mp) {
            this.mp = mp;
        }

        @Override
        public Class<? extends Annotation> annotationType() {
            return ((Annotation) mp).annotationType();
        }

        @Override
        public String value() {
            return mp.value();
        }

        @Override
        public String name() {
            return "";
        }

        @Override
        public String allowableValues() {
            return mp.allowableValues();
        }

        @Override
        public String access() {
            return mp.access();
        }

        @Override
        public String notes() {
            return mp.notes();
        }

        @Override
        public String dataType() {
            return "";
        }

        @Override
        public boolean required() {
            return mp.required();
        }

        @Override
        public int position() {
            return mp.position();
        }

        @Override
        public boolean hidden() {
            return mp.hidden();
        }

        @Override
        public String example() {
            return mp.example();
        }

        @Override
        public boolean readOnly() {
            return mp.readOnly();
        }

        @Override
        public String reference() {
            return mp.reference();
        }

        @Override
        public boolean allowEmptyValue() {
            return mp.allowEmptyValue();
        }
    }
}

package com.github.kongchen.swagger.docgen.spring;

public interface ParameterFilter {

    boolean isIgnoredParameter(ParameterMetadata parameterMetadata);
}

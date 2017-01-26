package com.github.kongchen.swagger.docgen.spring;

import com.google.common.collect.ImmutableSet;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.Principal;
import java.util.Set;

public class IgnoredClassParameterFilter implements ParameterFilter {

    public static final Set<Class<?>> SPRING_IGNORED_PARAMETER_TYPES = ImmutableSet.<Class<?>>builder()
            .add(Principal.class)
            .add(ModelAndView.class)
            .add(HttpServletRequest.class)
            .add(HttpServletResponse.class)
            .build();

    private final Set<Class<?>> ignoredParameterTypes;

    public IgnoredClassParameterFilter(Set<Class<?>> ignoredParameterTypes) {
        this.ignoredParameterTypes = ignoredParameterTypes;
    }

    @Override
    public boolean isIgnoredParameter(ParameterMetadata parameterMetadata) {
        return ignoredParameterTypes.contains(parameterMetadata.getType());
    }
}

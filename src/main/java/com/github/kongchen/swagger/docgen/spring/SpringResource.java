package com.github.kongchen.swagger.docgen.spring;

import org.apache.commons.lang3.StringUtils;

import com.github.kongchen.swagger.docgen.util.SpringUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * @author tedleman
 */
public class SpringResource {
    private Class<?> controllerClass;
    private List<Method> methods;
    private String controllerMapping; //FIXME should be an array
    private String resourceName;
    private String resourceKey;
    private String description;

    /**
     * @param clazz        (Class<?>) Controller class
     * @param resourceName resource Name
     */
    public SpringResource(final Class<?> clazz, final String resourceName, final String resourceKey, final String description) {
        this.controllerClass = clazz;
        this.resourceName = resourceName;
        this.resourceKey = resourceKey;
        this.description = description;
        methods = new ArrayList<Method>();

        final String[] controllerRequestMappingValues = SpringUtils.getControllerResquestMapping(controllerClass);

        this.controllerMapping = StringUtils.removeEnd(controllerRequestMappingValues[0], "/");
    }

    public Class<?> getControllerClass() {
        return controllerClass;
    }

    public void setControllerClass(final Class<?> controllerClass) {
        this.controllerClass = controllerClass;
    }

    public List<Method> getMethods() {
        return methods;
    }

    public void setMethods(final List<Method> methods) {
        this.methods = methods;
    }

    public void addMethod(final Method m) {
        this.methods.add(m);
    }

    public String getControllerMapping() {
        return controllerMapping;
    }

    public void setControllerMapping(final String controllerMapping) {
        this.controllerMapping = controllerMapping;
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResource(final String resource) {
        this.resourceName = resource;
    }

    public String getResourcePath() {
        return "/" + resourceName;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(final String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

}

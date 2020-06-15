package com.github.kongchen.swagger.docgen.spring;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * @author tedleman
 */
public class SpringResource {
    private Class<?> controllerClass;
    private List<Method> methods;
    private String controllerMapping;
    private String methodMapping;
    private String resourceKey;
    private String description;

    /**
     * @param clazz         Controller class
     * @param methodMapping resource Name
     * @param resourceKey   key containing the controller package, class controller class name, and controller-level @RequestMapping#value
     * @param description   description of the contrroller
     */
    public SpringResource(Class<?> clazz, String controllerMapping, String methodMapping, String resourceKey, String description) {
        this.controllerClass = clazz;
        this.controllerMapping = controllerMapping;
        this.methodMapping = methodMapping;
        this.resourceKey = resourceKey;
        this.description = description;
        methods = new ArrayList<Method>();
    }

    public Class<?> getControllerClass() {
        return controllerClass;
    }

    public void setControllerClass(Class<?> controllerClass) {
        this.controllerClass = controllerClass;
    }

    public List<Method> getMethods() {
        return methods;
    }

    public void setMethods(List<Method> methods) {
        this.methods = methods;
    }

    public void addMethod(Method m) {
        this.methods.add(m);
    }

    public String getControllerMapping() {
        return controllerMapping;
    }

    public void setControllerMapping(String controllerMapping) {
        this.controllerMapping = controllerMapping;
    }

    public String getMethodMapping() {
        return methodMapping;
    }

    public void setResource(String resource) {
        this.methodMapping = resource;
    }

    public String getResourcePath() {
        return "/" + methodMapping;
    }

    public String getResourceKey() {
        return resourceKey;
    }

    public void setResourceKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}

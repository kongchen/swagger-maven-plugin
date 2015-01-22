package com.github.kongchen.swagger.docgen.spring;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.springframework.web.bind.annotation.RequestMapping;

import com.github.kongchen.swagger.docgen.util.Utils;
/**
 * 
 * @author tedleman
 *
 */
public class SpringResource {
	private Class<?> controllerClass;
	private List<Method> methods;
	private String controllerMapping;
	private String resourceName;
	private String resourceKey;
	private String description;
	
	/**
	 * @param clazz (Class<?>) Controller class
	 * @param res (String) Resource Name
	 */
	public SpringResource(Class<?> clazz, String resourceName, String resourceKey, String description){
		this.controllerClass = clazz;
		this.resourceName = resourceName;
		this.resourceKey = resourceKey;
		this.description = description;
		methods = new ArrayList<Method>();
		
		RequestMapping req = controllerClass.getAnnotation(RequestMapping.class);
		String fullPath = req.value()[0];
		if(fullPath.endsWith("/")){
			fullPath = fullPath.substring(0,fullPath.length()-1);
		}
		this.controllerMapping = req.value()[0];

		String tempMapping=new String(this.controllerMapping);
		if(tempMapping.startsWith("/")){
			tempMapping = tempMapping.substring(1);
		}
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
	public void addMethod(Method m){
		this.methods.add(m);
	}
	public String getControllerMapping() {
		return controllerMapping;
	}
	public void setControllerMapping(String controllerMapping) {
		this.controllerMapping = controllerMapping;
	}
	public String getResourceName() {
		return resourceName;
	}
	public void setResource(String resource) {
		this.resourceName = resource;
	} 
	public String getResourcePath(){
		return "/"+resourceName;
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
package com.wordnik.jaxrs;

import javax.ws.rs.BeanParam;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.HeaderParam;

import io.swagger.annotations.ApiParam;

/**
 * Represents a nested {@code @BeanParam} target that is injected by constructor.
 * 
 * @see MyNestedBean
 */
public class MyConstructorInjectedNestedBean {
    
    private final String constructorInjectedHeader;
    
    /**
     * This bean param should be ignored because otherwise we would cycle
     * endlessly until stack overflow.
     */
    private final MyBean evilNestedBeanParamCycle;
    
    public MyConstructorInjectedNestedBean(
            
            @ApiParam("Header injected at constructor")
            @HeaderParam("constructorInjectedHeader")
            @DefaultValue("foo")
            String constructorInjectedHeader,
            
            @BeanParam MyBean evilNestedBeanParamCycle
    ) {
        this.constructorInjectedHeader = constructorInjectedHeader;
        this.evilNestedBeanParamCycle = evilNestedBeanParamCycle;
    }

    public String getMyNestedBeanHeader() {
        return constructorInjectedHeader;
    }
    
    public MyBean getEvilNestedBeanParamCycle() {
        return evilNestedBeanParamCycle;
    }
}

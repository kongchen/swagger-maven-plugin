package com.wordnik.jaxrs;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.HeaderParam;

import io.swagger.annotations.ApiParam;

/**
 * Represents a nested {@code @BeanParam} target that is injected by constructor.
 * 
 * @see MyNestedBean
 */
public class MyConstructorInjectedNestedBean {
    
    /**
     * Note: this property will not be found by
     * {@link com.github.kongchen.swagger.docgen.reader.SwaggerReader}, which
     * seems to be a limitation of {@link io.swagger.jaxrs.Reader} itself.
     */
    private final String constructorInjectedHeader;
    
    // @Inject would typically go here in real life, telling e.g. Jersey to use constructor injection
    public MyConstructorInjectedNestedBean(
            @ApiParam("Header injected at constructor")
            @HeaderParam("constructorInjectedHeader")
            @DefaultValue("foo")
            String constructorInjectedHeader
    ) {
        this.constructorInjectedHeader = constructorInjectedHeader;
    }

    public String getConstructorInjectedHeader() {
        return constructorInjectedHeader;
    }
}

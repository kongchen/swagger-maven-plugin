package com.github.kongchen.swagger.docgen.remote.model;

/**
 * Created by kongchen on 14/10/11.
 */
public interface CanBeSwaggerModel<SwaggerModel> {

    public SwaggerModel toSwaggerModel();

}

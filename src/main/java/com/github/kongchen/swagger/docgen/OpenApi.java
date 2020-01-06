package com.github.kongchen.swagger.docgen;

import io.swagger.models.Swagger;

public class OpenApi extends Swagger {
    private String openapi;

    public OpenApi(String version) {
        this.openapi = version;
        this.swagger = null;
    }

    public String getOpenapi() {
        return openapi;
    }

    public void setOpenapi(String openapi) {
        this.openapi = openapi;
    }
}

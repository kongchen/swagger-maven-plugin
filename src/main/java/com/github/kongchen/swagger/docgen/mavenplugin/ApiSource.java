package com.github.kongchen.swagger.docgen.mavenplugin;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: kongchen
 * Date: 3/7/13
 */
public class ApiSource {

    /**
     * @parameter
     */
    private List<String> apiClasses;

    /**
     * @parameter
     */
    private String apiPackage;

    /**
     * @parameter
     */
    private String apiVersion;

    /**
     * @parameter
     */
    private String basePath;


    /**
     * @parameter
     */
    private String outputTemplate;

    /**
     * @parameter
     */
    private String outputPath;


    public List<String> getApiClasses() {
        return apiClasses;
    }

    public void setApiClasses(List<String> apiClasses) {
        this.apiClasses = apiClasses;
    }

    public String getOutputTemplate() {
        return outputTemplate;
    }

    public void setOutputTemplate(String outputTemplate) {
        this.outputTemplate = outputTemplate;
    }

    public String getOutputPath() {
        return outputPath;
    }

    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }

    public String getApiPackage() {
        return apiPackage;
    }

    public void setApiPackage(String apiPackage) {
        this.apiPackage = apiPackage;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    public String getBasePath() {
        return basePath;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }
}

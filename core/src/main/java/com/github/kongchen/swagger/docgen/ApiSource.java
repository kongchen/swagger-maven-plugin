package com.github.kongchen.swagger.docgen;

import com.wordnik.swagger.annotations.Api;

import org.reflections.Reflections;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: kongchen
 * Date: 3/7/13
 */

public class ApiSource {

    /**
     * @parameter
     */
    private String locations;

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

    /**
     * @parameter
     */
    private String swaggerDirectory;

    public String mustacheFileRoot;

    public boolean useOutputFlatStructure = true;

    public Set<Class> getValidClasses() throws GenerateException {
        Set<Class> classes = new HashSet<Class>();
        if (getLocations() == null) {
            Set<Class<?>> c = new Reflections("").getTypesAnnotatedWith(Api.class);
            classes.addAll(c);
        } else {
            if (locations.contains(";")) {
                String[] sources = locations.split(";");
                for (String source : sources) {
                    Set<Class<?>> c = new Reflections(source).getTypesAnnotatedWith(Api.class);
                    classes.addAll(c);
                }
            } else {
                classes.addAll(new Reflections(locations).getTypesAnnotatedWith(Api.class));
            }
        }
        Iterator<Class> it = classes.iterator();
        while (it.hasNext()) {
            if (it.next().getName().startsWith("com.wordnik.swagger")) {
                it.remove();
            }
        }
        return classes;
    }

    public String getLocations() {
        return locations;
    }

    public void setLocations(String locations) {
        this.locations = locations;
    }

    public String getOutputTemplate() {
        return outputTemplate;
    }

    public void setOutputTemplate(String outputTemplate) {
        this.outputTemplate = outputTemplate;
    }

    public String getMustacheFileRoot() {
        return mustacheFileRoot;
    }

    public void setMustacheFileRoot(String mustacheFileRoot) {
        this.mustacheFileRoot = mustacheFileRoot;
    }

    public boolean isUseOutputFlatStructure() {
        return useOutputFlatStructure;
    }

    public void setUseOutputFlatStructure(boolean useOutputFlatStructure) {
        this.useOutputFlatStructure = useOutputFlatStructure;
    }

    public String getOutputPath() {
        return outputPath;
    }

    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
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

    public String getSwaggerDirectory() {
        return swaggerDirectory;
    }

    public void setSwaggerDirectory(String swaggerDirectory) {
        this.swaggerDirectory = swaggerDirectory;
    }

}

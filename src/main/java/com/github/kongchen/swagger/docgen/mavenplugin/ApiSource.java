package com.github.kongchen.swagger.docgen.mavenplugin;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.github.kongchen.swagger.docgen.GenerateException;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.models.Info;
import org.apache.maven.plugins.annotations.Parameter;
import org.reflections.Reflections;

/**
 * Created with IntelliJ IDEA.
 * User: kongchen
 * Date: 3/7/13
 */
public class ApiSource {

    /**
     * Java classes containing Swagger's annotation <code>@Api</code>, or Java packages containing those classes
     * can be configured here, use ; as the delimiter if you have more than one location.
     */
    @Parameter(required = true)
    private String locations;

    @Parameter(name = "info", required = true)
    private Info info;

    /**
     * The basePath of your APIs.
     */
    @Parameter(required = true)
    private String basePath;

    /**
     * The host (name or ip) serving the API.
     * This MUST be the host only and does not include the scheme nor sub-paths.
     * It MAY include a port. If the host is not included, the host serving the documentation
     * is to be used (including the port). The host does not support path templating.
     */
    private String host;

    /**
     * The transfer protocol of the API. Values MUST be from the list: "http", "https", "ws", "wss"
     * use ',' as delimiter
     */
    private String schemes;

    /**
     * <code>templatePath</code> is the path of a hbs template file,
     * see more details in next section.
     * If you don't want to generate extra api documents, just don't set it.
     */
    @Parameter(required = false)
    private String templatePath;

    @Parameter
    private String outputPath;

    @Parameter
    private String swaggerDirectory;

    @Parameter
    private String swaggerUIDocBasePath;

    @Parameter
    private String modelSubstitute;

    @Parameter
    private String apiSortComparator;

    /**
     * Information about swagger filter that will be used for prefiltering
     */
    @Parameter
    private String swaggerInternalFilter;

    @Parameter
    private String swaggerApiReader;

    @Parameter
    private boolean springmvc;

    @Parameter
    private String swaggerSchemaConverter;

    @Parameter
    private List<SecurityDefinition> securityDefinitions;


    public Set<Class<?>> getValidClasses() throws GenerateException {
        Set<Class<?>> classes = new HashSet<Class<?>>();
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
//        Iterator<Class> it = classes.iterator();
//        while (it.hasNext()) {
//            if (it.next().getName().startsWith("com.wordnik.swagger")) {
//                it.remove();
//            }
//        }
        return classes;
    }

    public List<SecurityDefinition> getSecurityDefinitions() {
        return securityDefinitions;
    }

    public void setSecurityDefinitions(List<SecurityDefinition> securityDefinitions) {
        this.securityDefinitions = securityDefinitions;
    }

    public Info getInfo() {
        return info;
    }

    public void setInfo(Info info) {
        this.info = info;
    }

    public String getLocations() {
        return locations;
    }

    public void setLocations(String locations) {
        this.locations = locations;
    }

    public String getTemplatePath() {
        return templatePath;
    }

    public void setTemplatePath(String templatePath) {
        this.templatePath = templatePath;
    }

    public String getOutputPath() {
        return outputPath;
    }

    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
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

    public void setSwaggerUIDocBasePath(String swaggerUIDocBasePath) {
        this.swaggerUIDocBasePath = swaggerUIDocBasePath;
    }

    public String getSwaggerUIDocBasePath() {
        return swaggerUIDocBasePath;
    }

    public String getHost() {
        return host;
    }

    public void setModelSubstitute(String modelSubstitute) {
        this.modelSubstitute = modelSubstitute;
    }

    public String getSwaggerInternalFilter() {
        return swaggerInternalFilter;
    }

    public void setSwaggerInternalFilter(String swaggerInternalFilter) {
        this.swaggerInternalFilter = swaggerInternalFilter;
    }

    public String getSwaggerApiReader() {
        return swaggerApiReader;
    }

    public void setSwaggerApiReader(String swaggerApiReader) {
        this.swaggerApiReader = swaggerApiReader;
    }

    public String getApiSortComparator() {
        return apiSortComparator;
    }

    public void setApiSortComparator(String apiSortComparator) {
        this.apiSortComparator = apiSortComparator;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getSchemes() {
        return schemes;
    }

    public void setSchemes(String schemes) {
        this.schemes = schemes;
    }

    public String getModelSubstitute() {
        return modelSubstitute;
    }

    public boolean isSpringmvc() {
        return springmvc;
    }

    public void setSpringmvc(boolean springmvc) {
        this.springmvc = springmvc;
    }

    public String getSwaggerSchemaConverter() {
        return swaggerSchemaConverter;
    }

    public void setSwaggerSchemaConverter(String swaggerSchemaConverter) {
        this.swaggerSchemaConverter = swaggerSchemaConverter;
    }
}


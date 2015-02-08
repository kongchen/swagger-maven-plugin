package com.github.kongchen.swagger.docgen.mavenplugin;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.github.kongchen.swagger.docgen.GenerateException;
import com.wordnik.swagger.annotations.Api;
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
    
    @Parameter(name = "apiInfo", required = false)
    private ApiSourceInfo apiInfo;

    /**
     * The version of your APIs 
     */
    @Parameter(required = true)
    private String apiVersion;

    /**
     * The basePath of your APIs. 
     */
    @Parameter(required = true)
    private String basePath;

    /**
     * <code>outputTemplate</code> is the path of a mustache template file, 
     * see more details in next section. 
     * If you don't want to generate extra api documents, just don't set it.
     */
    @Parameter(required = false)
    private String outputTemplate;

    @Parameter
    private String outputPath;

    @Parameter
    private String swaggerDirectory;

    @Parameter
    public String mustacheFileRoot;

    @Parameter
    public boolean useOutputFlatStructure = true;

    @Parameter
    private String swaggerUIDocBasePath;

    @Parameter
    private String overridingModels;

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
	private String swaggerDocumentSource;
    
    @Parameter
    private boolean supportSpringMvc;

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

    public ApiSourceInfo getApiInfo() {
        return apiInfo;
    }

    public void setApiInfo(ApiSourceInfo apiInfo) {
        this.apiInfo = apiInfo;
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

    public void setSwaggerUIDocBasePath(String swaggerUIDocBasePath) {
        this.swaggerUIDocBasePath = swaggerUIDocBasePath;
    }

    public String getSwaggerUIDocBasePath() {
        return swaggerUIDocBasePath;
    }

    public String getOverridingModels() {
        return overridingModels;
    }

    public void setOverridingModels(String overridingModels) {
        this.overridingModels = overridingModels;
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

  public String getSwaggerDocumentSource() {
    return swaggerDocumentSource;
  }

  public void setSwaggerDocumentSource(String swaggerDocumentSource) {
    this.swaggerDocumentSource = swaggerDocumentSource;
  }

    public boolean isSupportSpringMvc() {
        return supportSpringMvc;
    }

    public void setSupportSpringMvc(boolean supportSpringMvc) {
        this.supportSpringMvc = supportSpringMvc;
    }
}

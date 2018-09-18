package com.github.kongchen.swagger.docgen.mavenplugin;

import io.swagger.annotations.SwaggerDefinition;
import io.swagger.models.Contact;
import io.swagger.models.Info;
import io.swagger.models.License;
import io.swagger.util.BaseReaderUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugins.annotations.Parameter;
import org.reflections.Reflections;
import org.springframework.core.annotation.AnnotationUtils;

import java.io.File;
import java.lang.annotation.Annotation;
import java.util.*;

/**
 * User: kongchen
 * Date: 3/7/13
 */
public class ApiSource {
    /**
     * Java classes containing Swagger's annotation <code>@Api</code>, or Java packages containing those classes
     * can be configured here.
     */
    @Parameter(required = true)
    private List<String> locations;

    @Parameter
    private Info info;

    /**
     * The basePath of your APIs.
     */
    @Parameter
    private String basePath;

    @Parameter(defaultValue = "false")
    private boolean removeBasePathFromEndpoints;

    /**
     * The host (name or ip) serving the API.
     * This MUST be the host only and does not include the scheme nor sub-paths.
     * It MAY include a port. If the host is not included, the host serving the documentation
     * is to be used (including the port). The host does not support path templating.
     */
    private String host;

    /*
     * The transfer protocols of the API. Values MUST be from the list: "http", "https", "ws", "wss"
     */
    private List<String> schemes;

    /**
     * <code>templatePath</code> is the path of a hbs template file,
     * see more details in next section.
     * If you don't want to generate extra api documents, just don't set it.
     */
    @Parameter
    private String templatePath;

    @Parameter
    private String outputPath;

    @Parameter(defaultValue = "json")
    private String outputFormats;

    @Parameter
    private String swaggerDirectory;

    @Parameter
    private String swaggerFileName;

    /**
     * <code>attachSwaggerArtifact</code> triggers plugin execution to attach the generated
     * swagger.json to Maven session for deployment purpose.  The attached classifier
     * is the directory name of <code>swaggerDirectory</code>
     */
    @Parameter
    private boolean attachSwaggerArtifact;

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

    /**
     * List of full qualified class names of SwaggerExtension implementations to be
     * considered for the generation
     */
    @Parameter
    private List<String> swaggerExtensions;

    @Parameter
    private boolean springmvc;

    @Parameter
    private boolean useJAXBAnnotationProcessor;

    @Parameter
    private boolean useJAXBAnnotationProcessorAsPrimary = true;

    @Parameter
    private String swaggerSchemaConverter;

    @Parameter
    private List<SecurityDefinition> securityDefinitions;

    @Parameter
    private List<String> typesToSkip = new ArrayList<String>();

    @Parameter
    private List<String> apiModelPropertyAccessExclusions = new ArrayList<String>();

    @Parameter
    private boolean jsonExampleValues = false;

    @Parameter
    private File descriptionFile;

    @Parameter
    private List<String> modelConverters;
    
    @Parameter
    private boolean skipInheritingClasses = false;
    
    @Parameter
    private String operationIdFormat;

    public Set<Class<?>> getValidClasses(Class<? extends Annotation> clazz) {
        Set<Class<?>> classes = new LinkedHashSet<Class<?>>();
        
        List<String> prefixes = new ArrayList<String>();
        if (getLocations() == null) {
            prefixes.add("");
        } else {
            prefixes.addAll(getLocations());
        }
        
        for (String location : prefixes) {
            Set<Class<?>> c = new Reflections(location).getTypesAnnotatedWith(clazz, true);
            classes.addAll(c);

            if (!skipInheritingClasses) {
                Set<Class<?>> inherited = new Reflections(location).getTypesAnnotatedWith(clazz);
                classes.addAll(inherited);
            }
        }
        return classes;
    }

    public List<String> getApiModelPropertyAccessExclusions() {
        return apiModelPropertyAccessExclusions;
    }

    public void setApiModelPropertyExclusions(List<String> apiModelPropertyAccessExclusions) {
        this.apiModelPropertyAccessExclusions = apiModelPropertyAccessExclusions;
    }

    public List<SecurityDefinition> getSecurityDefinitions() {
        return securityDefinitions;
    }

    public List<String> getTypesToSkip() {
        return typesToSkip;
    }

    public void setTypesToSkip(List<String> typesToSkip) {
        this.typesToSkip = typesToSkip;
    }

    public void setSecurityDefinitions(List<SecurityDefinition> securityDefinitions) {
        this.securityDefinitions = securityDefinitions;
    }

    public Info getInfo() {
        if (info == null) {
            setInfoFromAnnotation();
        }
        return info;
    }

    private void setInfoFromAnnotation() {
        Info resultInfo = new Info();
        for (Class<?> aClass : getValidClasses(SwaggerDefinition.class)) {
            SwaggerDefinition swaggerDefinition = AnnotationUtils.findAnnotation(aClass, SwaggerDefinition.class);
            io.swagger.annotations.Info infoAnnotation = swaggerDefinition.info();

            Info info = new Info().title(infoAnnotation.title())
                    .description(emptyToNull(infoAnnotation.description()))
                    .version(infoAnnotation.version())
                    .termsOfService(emptyToNull(infoAnnotation.termsOfService()))
                    .license(from(infoAnnotation.license()))
                    .contact(from(infoAnnotation.contact()));

            Map<String, Object> customExtensions = BaseReaderUtils.parseExtensions(infoAnnotation.extensions());
            for (Map.Entry<String, Object> extension : customExtensions.entrySet()) {
                resultInfo.setVendorExtension(extension.getKey(), extension.getValue());
            }

            resultInfo.mergeWith(info);
        }
        info = resultInfo;
    }

    private Contact from(io.swagger.annotations.Contact contactAnnotation) {
        Contact contact = new Contact()
                .name(emptyToNull(contactAnnotation.name()))
                .email(emptyToNull(contactAnnotation.email()))
                .url(emptyToNull(contactAnnotation.url()));
        if (contact.getName() == null && contact.getEmail() == null && contact.getUrl() == null) {
            contact = null;
        }
        return contact;
    }

    private License from(io.swagger.annotations.License licenseAnnotation) {
        License license = new License()
                .name(emptyToNull(licenseAnnotation.name()))
                .url(emptyToNull(licenseAnnotation.url()));
        if (license.getName() == null && license.getUrl() == null) {
            license = null;
        }
        return license;
    }

    private void setBasePathFromAnnotation() {
        for (Class<?> aClass : getValidClasses(SwaggerDefinition.class)) {
            SwaggerDefinition swaggerDefinition = AnnotationUtils.findAnnotation(aClass, SwaggerDefinition.class);
            basePath = emptyToNull(swaggerDefinition.basePath());
        }
    }

    private void setHostFromAnnotation() {
        for (Class<?> aClass : getValidClasses(SwaggerDefinition.class)) {
            SwaggerDefinition swaggerDefinition = AnnotationUtils.findAnnotation(aClass, SwaggerDefinition.class);
            host = emptyToNull(swaggerDefinition.host());
        }
    }

    public void setInfo(Info info) {
        this.info = info;
    }

    public List<String> getLocations() {
        return locations;
    }

    public void setLocations(List<String> locations) {
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

    public String getOutputFormats() {
        return outputFormats;
    }

    public void setOutputFormats(String outputFormats) {
        this.outputFormats = outputFormats;
    }

    public String getBasePath() {
        if (basePath == null) {
            setBasePathFromAnnotation();
        }
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

    public String getSwaggerFileName() {
        return swaggerFileName;
    }

    public void setSwaggerFileName(String swaggerFileName) {
        this.swaggerFileName = swaggerFileName;
    }

    public boolean isAttachSwaggerArtifact() {
        return attachSwaggerArtifact;
    }

    public void setAttachSwaggerArtifact(boolean attachSwaggerArtifact) {
        this.attachSwaggerArtifact = attachSwaggerArtifact;
    }

    public void setSwaggerUIDocBasePath(String swaggerUIDocBasePath) {
        this.swaggerUIDocBasePath = swaggerUIDocBasePath;
    }

    public String getSwaggerUIDocBasePath() {
        return swaggerUIDocBasePath;
    }

    public String getHost() {
        if (host == null) {
            setHostFromAnnotation();
        }
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

    public List<String> getSwaggerExtensions() {
		return swaggerExtensions;
	}

	public void setSwaggerExtensions(List<String> swaggerExtensions) {
		this.swaggerExtensions = swaggerExtensions;
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

    public List<String> getSchemes() {
        return schemes;
    }

    public void setSchemes(List<String> schemes) {
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

    public boolean isJsonExampleValues() {
        return jsonExampleValues;
    }

    public void setJsonExampleValues(boolean jsonExampleValues) {
        this.jsonExampleValues = jsonExampleValues;
    }

    public boolean isUseJAXBAnnotationProcessor() {
        return useJAXBAnnotationProcessor;
    }

    public void setUseJAXBAnnotationProcessor(boolean useJAXBAnnotationProcessor) {
        this.useJAXBAnnotationProcessor = useJAXBAnnotationProcessor;
    }

    public boolean isUseJAXBAnnotationProcessorAsPrimary() {
        return useJAXBAnnotationProcessorAsPrimary;
    }

    public void setUseJAXBAnnotationProcessorAsPrimary(boolean useJAXBAnnotationProcessorAsPrimary) {
        this.useJAXBAnnotationProcessorAsPrimary = useJAXBAnnotationProcessorAsPrimary;
    }

    public File getDescriptionFile() {
        return descriptionFile;
    }

    public void setDescriptionFile(File descriptionFile) {
        this.descriptionFile = descriptionFile;
    }

    public List<String> getModelConverters() {
        return modelConverters;
    }

    public void setModelConverters(List<String> modelConverters) {
        this.modelConverters = modelConverters;
    }

    public String getOperationIdFormat() {
		return operationIdFormat;
	}

	public void setOperationIdFormat(String operationIdFormat) {
		this.operationIdFormat = operationIdFormat;
	}

	private String emptyToNull(String str) {
        return StringUtils.isEmpty(str) ? null : str;
    }

    public Boolean getRemoveBasePathFromEndpoints() {
        return removeBasePathFromEndpoints;
    }

    public void setRemoveBasePathFromEndpoints(Boolean removeBasePathFromEndpoints) {
        this.removeBasePathFromEndpoints = removeBasePathFromEndpoints;
    }
}


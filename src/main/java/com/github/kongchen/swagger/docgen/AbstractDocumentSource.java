package com.github.kongchen.swagger.docgen;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule.Priority;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.helper.StringHelpers;
import com.github.jknack.handlebars.io.TemplateLoader;
import com.github.kongchen.swagger.docgen.mavenplugin.ApiSource;
import com.github.kongchen.swagger.docgen.mavenplugin.SecurityDefinition;
import com.github.kongchen.swagger.docgen.reader.AbstractReader;
import com.github.kongchen.swagger.docgen.reader.ClassSwaggerReader;
import com.github.kongchen.swagger.docgen.reader.ModelModifier;
import io.swagger.annotations.Api;
import io.swagger.config.FilterFactory;
import io.swagger.converter.ModelConverter;
import io.swagger.converter.ModelConverters;
import io.swagger.core.filter.SpecFilter;
import io.swagger.core.filter.SwaggerSpecFilter;
import io.swagger.jaxrs.ext.SwaggerExtension;
import io.swagger.jaxrs.ext.SwaggerExtensions;
import io.swagger.models.Path;
import io.swagger.models.Scheme;
import io.swagger.models.Swagger;
import io.swagger.models.auth.SecuritySchemeDefinition;
import io.swagger.models.properties.Property;
import io.swagger.util.Json;
import io.swagger.util.Yaml;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.nio.charset.Charset;
import java.util.*;

import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 * @author chekong 05/13/2013
 */
public abstract class AbstractDocumentSource {
    protected final ApiSource apiSource;
    protected final Log LOG;
    protected final List<Type> typesToSkip = new ArrayList<Type>();
    protected Swagger swagger;
    protected String swaggerSchemaConverter;
    private final String outputPath;
    private final String templatePath;
    private final String swaggerPath;
    private final String modelSubstitute;
    private final boolean jsonExampleValues;
    private ObjectMapper mapper = Json.mapper();
    private boolean isSorted = false;
    protected String encoding = "UTF-8";

    public AbstractDocumentSource(Log log, ApiSource apiSource) throws MojoFailureException {
        LOG = log;
        this.outputPath = apiSource.getOutputPath();
        this.templatePath = apiSource.getTemplatePath();
        this.swaggerPath = apiSource.getSwaggerDirectory();
        this.modelSubstitute = apiSource.getModelSubstitute();
        this.jsonExampleValues = apiSource.isJsonExampleValues();

        swagger = new Swagger();
        if (apiSource.getSchemes() != null) {
            for (String scheme : apiSource.getSchemes()) {
                swagger.scheme(Scheme.forValue(scheme));
            }
        }

        // read description from file
        if (apiSource.getDescriptionFile() != null) {
            try {
                InputStream is = new FileInputStream(apiSource.getDescriptionFile());
                apiSource.getInfo().setDescription(IOUtils.toString(is));
                is.close();
            } catch (IOException e) {
                throw new MojoFailureException(e.getMessage(), e);
            }
        }

        swagger.setHost(apiSource.getHost());
        swagger.setInfo(apiSource.getInfo());
        swagger.setBasePath(apiSource.getBasePath());

        this.apiSource = apiSource;
    }

    public void loadDocuments() throws GenerateException {
        ClassSwaggerReader reader = resolveApiReader();

        loadSwaggerExtensions(apiSource);

        swagger = reader.read(getValidClasses());

        swagger = removeBasePathFromEndpoints(swagger, apiSource.getRemoveBasePathFromEndpoints());

        swagger = addSecurityDefinitions(swagger, apiSource);

        swagger = doFilter(swagger);
    }

    private Swagger doFilter(Swagger swagger) throws GenerateException {
        String filterClassName = apiSource.getSwaggerInternalFilter();
        if (filterClassName != null) {
            try {
                LOG.debug(String.format("Setting filter configuration: %s", filterClassName));
                FilterFactory.setFilter((SwaggerSpecFilter) Class.forName(filterClassName).newInstance());
            } catch (Exception e) {
                throw new GenerateException("Cannot load: " + filterClassName, e);
            }
        }

        SwaggerSpecFilter filter = FilterFactory.getFilter();
        if (filter == null) {
            return swagger;
        }
        return new SpecFilter().filter(
                swagger,
                filter,
                new HashMap<String, List<String>>(),
                new HashMap<String, String>(),
                new HashMap<String, List<String>>());
    }

    private Swagger addSecurityDefinitions(final Swagger swagger, ApiSource apiSource) throws GenerateException {
        Swagger result = swagger;
        if (apiSource.getSecurityDefinitions() == null) {
            return result;
        }
        Map<String, SecuritySchemeDefinition> definitions = new TreeMap<String, SecuritySchemeDefinition>();
        for (SecurityDefinition sd : apiSource.getSecurityDefinitions()) {
            for (Map.Entry<String, SecuritySchemeDefinition> entry : sd.generateSecuritySchemeDefinitions().entrySet()) {
                definitions.put(entry.getKey(), entry.getValue());
            }
        }
        result.setSecurityDefinitions(definitions);
        return result;
    }

    /**
     * The reader may modify the extensions list, therefore add the additional swagger extensions
     * after the instantiation of the reader
     */
    private void loadSwaggerExtensions(ApiSource apiSource) throws GenerateException {
        if (apiSource.getSwaggerExtensions() != null) {
            List<SwaggerExtension> extensions = SwaggerExtensions.getExtensions();
            extensions.addAll(resolveSwaggerExtensions());
        }
    }

    public void toSwaggerDocuments(String uiDocBasePath, String outputFormats, String encoding) throws GenerateException {
        toSwaggerDocuments(uiDocBasePath, outputFormats, null, encoding);
    }

    public void toSwaggerDocuments(String uiDocBasePath, String outputFormats, String fileName, String encoding) throws GenerateException {
        mapper.configure(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS, false);

        mapper = mapper.copy();

        if (jsonExampleValues) {
            mapper.addMixInAnnotations(Property.class, PropertyExampleMixIn.class);
        }

        if (swaggerPath == null) {
            return;
        }

        if (!isSorted) {
            Utils.sortSwagger(swagger);
            isSorted = true;
        }

        File dir = new File(swaggerPath);
        if (dir.isFile()) {
            throw new GenerateException(String.format("Swagger-outputDirectory[%s] must be a directory!", swaggerPath));
        }

        if (!dir.exists()) {
            try {
                FileUtils.forceMkdir(dir);
            } catch (IOException e) {
                throw new GenerateException(String.format("Create Swagger-outputDirectory[%s] failed.", swaggerPath));
            }
        }

        fileName = defaultString(fileName, "swagger");

        for (String format : outputFormats.split(",")) {
            try {
                Output output = Output.valueOf(format.toLowerCase());
                switch (output) {
                    case json:
                        ObjectWriter jsonWriter = mapper.writer(new DefaultPrettyPrinter());
                        FileUtils.write(new File(dir, fileName + ".json"), jsonWriter.writeValueAsString(swagger), encoding);
                        break;
                    case yaml:
                        FileUtils.write(new File(dir, fileName + ".yaml"), Yaml.pretty().writeValueAsString(swagger), encoding);
                        break;
                }
            } catch (Exception e) {
                throw new GenerateException(String.format("Declared output format [%s] is not supported.", format));
            }
        }
    }

    public void loadModelModifier() throws GenerateException, IOException {
        ObjectMapper objectMapper = Json.mapper();
        if (apiSource.isUseJAXBAnnotationProcessor()) {
            JaxbAnnotationModule jaxbAnnotationModule = new JaxbAnnotationModule();
            if (apiSource.isUseJAXBAnnotationProcessorAsPrimary()) {
                jaxbAnnotationModule.setPriority(Priority.PRIMARY);
            } else {
                jaxbAnnotationModule.setPriority(Priority.SECONDARY);
            }
            objectMapper.registerModule(jaxbAnnotationModule);

            // to support @ApiModel on class level.
            // must be registered only if we use JaxbAnnotationModule before. Why?
            objectMapper.registerModule(new EnhancedSwaggerModule());
        }
        ModelModifier modelModifier = new ModelModifier(objectMapper);

        List<String> apiModelPropertyAccessExclusions = apiSource.getApiModelPropertyAccessExclusions();
        if (apiModelPropertyAccessExclusions != null && !apiModelPropertyAccessExclusions.isEmpty()) {
            modelModifier.setApiModelPropertyAccessExclusions(apiModelPropertyAccessExclusions);
        }

        if (modelSubstitute != null) {
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(this.modelSubstitute)));
                String line = reader.readLine();
                while (line != null) {
                    String[] classes = line.split(":");
                    if (classes.length != 2) {
                        throw new GenerateException("Bad format of override model file, it should be ${actualClassName}:${expectClassName}");
                    }
                    modelModifier.addModelSubstitute(classes[0].trim(), classes[1].trim());
                    line = reader.readLine();
                }
            } catch (IOException e) {
                throw new GenerateException(e);
            } finally {
                if (reader != null) {
                    reader.close();
                }
            }
        }

        ModelConverters.getInstance().addConverter(modelModifier);
    }

    public void loadModelConverters() throws MojoExecutionException {
        final List<String> modelConverters = apiSource.getModelConverters();
        if (modelConverters == null) {
            return;
        }

        for (String modelConverter : modelConverters) {
            try {
                final Class<?> modelConverterClass = Class.forName(modelConverter);
                if (ModelConverter.class.isAssignableFrom(modelConverterClass)) {
                    final ModelConverter modelConverterInstance = (ModelConverter) modelConverterClass.newInstance();
                    ModelConverters.getInstance().addConverter(modelConverterInstance);
                } else {
                    throw new MojoExecutionException(String.format("Class %s has to be a subclass of %s", modelConverterClass.getName(), ModelConverter.class));
                }
            } catch (ClassNotFoundException e) {
                throw new MojoExecutionException(String.format("Could not find custom model converter %s", modelConverter), e);
            } catch (InstantiationException e) {
                throw new MojoExecutionException(String.format("Unable to instantiate custom model converter %s", modelConverter), e);
            } catch (IllegalAccessException e) {
                throw new MojoExecutionException(String.format("Unable to instantiate custom model converter %s", modelConverter), e);
            }
        }
    }

    public void loadTypesToSkip() throws GenerateException {
        List<String> typesToSkip = apiSource.getTypesToSkip();
        if (typesToSkip == null) {
            return;
        }
        for (String typeToSkip : typesToSkip) {
            try {
                Type type = Class.forName(typeToSkip);
                this.typesToSkip.add(type);
            } catch (ClassNotFoundException e) {
                throw new GenerateException(e);
            }
        }
    }

    protected Swagger removeBasePathFromEndpoints(Swagger swagger, boolean removeBasePathFromEndpoints) {
        Swagger result = swagger;
        if (!removeBasePathFromEndpoints) {
            return result;
        }
        String basePath = swagger.getBasePath();
        if (isEmpty(basePath)) {
            return result;
        }
        Map<String, Path> oldPathMap = result.getPaths();
        Map<String, Path> newPathMap = new HashMap<String, Path>();
        for (Map.Entry<String, Path> entry: oldPathMap.entrySet()) {
            newPathMap.put(entry.getKey().replace(basePath, ""), entry.getValue());
        }
        result.setPaths(newPathMap);
        return result;
    }

    protected File createFile(File dir, String outputResourcePath) throws IOException {
        File serviceFile;
        int i = outputResourcePath.lastIndexOf("/");
        if (i != -1) {
            String fileName = outputResourcePath.substring(i + 1);
            String subDir = outputResourcePath.substring(0, i);
            File finalDirectory = new File(dir, subDir);
            finalDirectory.mkdirs();
            serviceFile = new File(finalDirectory, fileName);
        } else {
            serviceFile = new File(dir, outputResourcePath);
        }
        while (!serviceFile.createNewFile()) {
            serviceFile.delete();
        }
        LOG.info("Creating file " + serviceFile.getAbsolutePath());
        return serviceFile;
    }

    public void toDocuments() throws GenerateException {
        if (!isSorted) {
            Utils.sortSwagger(swagger);
            isSorted = true;
        }
        LOG.info("Writing doc to " + outputPath + "...");

        try {
            FileOutputStream fileOutputStream = new FileOutputStream(outputPath);
            OutputStreamWriter writer = new OutputStreamWriter(fileOutputStream, Charset.forName("UTF-8"));

            TemplatePath tp = Utils.parseTemplateUrl(templatePath);

            Handlebars handlebars = new Handlebars(tp.loader);
            initHandlebars(handlebars);

            Template template = handlebars.compile(tp.name);

            template.apply(swagger, writer);
            writer.close();
            LOG.info("Done!");
        } catch (MalformedURLException e) {
            throw new GenerateException(e);
        } catch (IOException e) {
            throw new GenerateException(e);
        }
    }

    private void initHandlebars(Handlebars handlebars) {
        handlebars.registerHelper("ifeq", new Helper<String>() {
            @Override
            public CharSequence apply(String value, Options options) throws IOException {
                if (value == null || options.param(0) == null) {
                    return options.inverse();
                }
                if (value.equals(options.param(0))) {
                    return options.fn();
                }
                return options.inverse();
            }
        });

        handlebars.registerHelper("basename", new Helper<String>() {
            @Override
            public CharSequence apply(String value, Options options) throws IOException {
                if (value == null) {
                    return null;
                }
                int lastSlash = value.lastIndexOf("/");
                if (lastSlash == -1) {
                    return value;
                } else {
                    return value.substring(lastSlash + 1);
                }
            }
        });

        handlebars.registerHelper(StringHelpers.join.name(), StringHelpers.join);
        handlebars.registerHelper(StringHelpers.lower.name(), StringHelpers.lower);
    }

    /**
     * Resolves the API reader which should be used to scan the classes.
     *
     * @return ClassSwaggerReader to use
     * @throws GenerateException if the reader cannot be created / resolved
     */
    protected abstract ClassSwaggerReader resolveApiReader() throws GenerateException;

    /**
     * Returns the set of classes which should be included in the scanning.
     *
     * @return Set containing all valid classes
     */
    protected Set<Class<?>> getValidClasses() {
        return apiSource.getValidClasses(Api.class);
    }

    /**
     * Resolves all {@link SwaggerExtension} instances configured to be added to the Swagger configuration.
     *
     * @return List of {@link SwaggerExtension} which should be added to the swagger configuration
     * @throws GenerateException if the swagger extensions could not be created / resolved
     */
    protected List<SwaggerExtension> resolveSwaggerExtensions() throws GenerateException {
        List<String> clazzes = apiSource.getSwaggerExtensions();
        List<SwaggerExtension> resolved = new ArrayList<SwaggerExtension>();
        if (clazzes != null) {
            for (String clazz : clazzes) {
                SwaggerExtension extension;
                try {
                    extension = (SwaggerExtension) Class.forName(clazz).newInstance();
                } catch (Exception e) {
                    throw new GenerateException("Cannot load Swagger extension: " + clazz, e);
                }
                resolved.add(extension);
            }
        }
        return resolved;
    }

    protected ClassSwaggerReader getCustomApiReader(String customReaderClassName) throws GenerateException {
        try {
            LOG.info("Reading custom API reader: " + customReaderClassName);
            Class<?> clazz = Class.forName(customReaderClassName);
            if (AbstractReader.class.isAssignableFrom(clazz)) {
                Constructor<?> constructor = clazz.getConstructor(Swagger.class, Log.class);
                return (ClassSwaggerReader) constructor.newInstance(swagger, LOG);
            } else {
                return (ClassSwaggerReader) clazz.newInstance();
            }
        } catch (Exception e) {
            throw new GenerateException("Cannot load Swagger API reader: " + customReaderClassName, e);
        }
    }
}

enum Output {
    json,
    yaml
}

class TemplatePath {
    String prefix;
    String name;
    String suffix;
    public TemplateLoader loader;
}

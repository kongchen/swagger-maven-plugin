package com.github.kongchen.swagger.docgen;

import com.fasterxml.jackson.annotation.JsonInclude;
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
    private final ObjectMapper mapper = new ObjectMapper();
    private boolean isSorted = false;
    protected String encoding = "UTF-8";

    public AbstractDocumentSource(final Log log, final ApiSource apiSource) throws MojoFailureException {
        LOG = log;
        this.outputPath = apiSource.getOutputPath();
        this.templatePath = apiSource.getTemplatePath();
        this.swaggerPath = apiSource.getSwaggerDirectory();
        this.modelSubstitute = apiSource.getModelSubstitute();
        this.jsonExampleValues = apiSource.isJsonExampleValues();

        swagger = new Swagger();
        if (apiSource.getSchemes() != null) {
            for (final String scheme : apiSource.getSchemes()) {
                swagger.scheme(Scheme.forValue(scheme));
            }
        }

        // read description from file
        if (apiSource.getDescriptionFile() != null) {
            try {
                final InputStream is = new FileInputStream(apiSource.getDescriptionFile());
                apiSource.getInfo().setDescription(IOUtils.toString(is));
                is.close();
            } catch (final IOException e) {
                throw new MojoFailureException(e.getMessage(), e);
            }
        }

        swagger.setHost(apiSource.getHost());
        swagger.setInfo(apiSource.getInfo());
        swagger.setBasePath(apiSource.getBasePath());

        this.apiSource = apiSource;
    }

    public void loadDocuments() throws GenerateException {
        final ClassSwaggerReader reader = resolveApiReader();

        loadSwaggerExtensions(apiSource);

        swagger = reader.read(getValidClasses());

        swagger = addSecurityDefinitions(swagger, apiSource);

        swagger = doFilter(swagger);
    }

    private Swagger doFilter(final Swagger swagger) throws GenerateException {
        final String filterClassName = apiSource.getSwaggerInternalFilter();
        if (filterClassName != null) {
            try {
                LOG.debug(String.format("Setting filter configuration: %s", filterClassName));
                FilterFactory.setFilter((SwaggerSpecFilter) Class.forName(filterClassName).newInstance());
            } catch (final Exception e) {
                throw new GenerateException("Cannot load: " + filterClassName, e);
            }
        }

        final SwaggerSpecFilter filter = FilterFactory.getFilter();
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

    private Swagger addSecurityDefinitions(final Swagger swagger, final ApiSource apiSource) throws GenerateException {
        final Swagger result = swagger;
        if (apiSource.getSecurityDefinitions() == null) {
            return result;
        }
        final Map<String, SecuritySchemeDefinition> definitions = new TreeMap<String, SecuritySchemeDefinition>();
        for (final SecurityDefinition sd : apiSource.getSecurityDefinitions()) {
            for (final Map.Entry<String, SecuritySchemeDefinition> entry : sd.generateSecuritySchemeDefinitions().entrySet()) {
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
    private void loadSwaggerExtensions(final ApiSource apiSource) throws GenerateException {
        if (apiSource.getSwaggerExtensions() != null) {
            final List<SwaggerExtension> extensions = SwaggerExtensions.getExtensions();
            extensions.addAll(resolveSwaggerExtensions());
        }
    }

    public void toSwaggerDocuments(final String uiDocBasePath, final String outputFormats, final String encoding) throws GenerateException {
        toSwaggerDocuments(uiDocBasePath, outputFormats, null, encoding);
    }

    public void toSwaggerDocuments(final String uiDocBasePath, final String outputFormats, String fileName, final String encoding) throws GenerateException {
        mapper.configure(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS, false);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

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
        final File dir = new File(swaggerPath);
        if (dir.isFile()) {
            throw new GenerateException(String.format("Swagger-outputDirectory[%s] must be a directory!", swaggerPath));
        }

        if (!dir.exists()) {
            try {
                FileUtils.forceMkdir(dir);
            } catch (final IOException e) {
                throw new GenerateException(String.format("Create Swagger-outputDirectory[%s] failed.", swaggerPath));
            }
        }

        if (fileName == null || "".equals(fileName.trim())) {
            fileName = "swagger";
        }
        try {
            if (outputFormats != null) {
                for (final String format : outputFormats.split(",")) {
                    try {
                        final Output output = Output.valueOf(format.toLowerCase());
                        switch (output) {
                            case json:
                                final ObjectWriter jsonWriter = mapper.writer(new DefaultPrettyPrinter().withoutSpacesInObjectEntries());
                                FileUtils.write(new File(dir, fileName + ".json"), jsonWriter.writeValueAsString(swagger), encoding);
                                break;
                            case yaml:
                                FileUtils.write(new File(dir, fileName + ".yaml"), Yaml.pretty().writeValueAsString(swagger), encoding);
                                break;
                        }
                    } catch (final Exception e) {
                        throw new GenerateException(String.format("Declared output format [%s] is not supported.", format));
                    }
                }
            } else {
                // Default to json
                final ObjectWriter jsonWriter = mapper.writer(new DefaultPrettyPrinter());
                FileUtils.write(new File(dir, fileName + ".json"), jsonWriter.writeValueAsString(swagger), encoding);
            }
        } catch (final IOException e) {
            throw new GenerateException(e);
        }
    }

    public void loadModelModifier() throws GenerateException, IOException {
        final ObjectMapper objectMapper = Json.mapper();
        if (apiSource.isUseJAXBAnnotationProcessor()) {
            final JaxbAnnotationModule jaxbAnnotationModule = new JaxbAnnotationModule();
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
        final ModelModifier modelModifier = new ModelModifier(objectMapper);

        final List<String> apiModelPropertyAccessExclusions = apiSource.getApiModelPropertyAccessExclusions();
        if (apiModelPropertyAccessExclusions != null && !apiModelPropertyAccessExclusions.isEmpty()) {
            modelModifier.setApiModelPropertyAccessExclusions(apiModelPropertyAccessExclusions);
        }

        if (modelSubstitute != null) {
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(this.modelSubstitute)));
                String line = reader.readLine();
                while (line != null) {
                    final String[] classes = line.split(":");
                    if (classes.length != 2) {
                        throw new GenerateException("Bad format of override model file, it should be ${actualClassName}:${expectClassName}");
                    }
                    modelModifier.addModelSubstitute(classes[0].trim(), classes[1].trim());
                    line = reader.readLine();
                }
            } catch (final IOException e) {
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

        for (final String modelConverter : modelConverters) {
            try {
                final Class<?> modelConverterClass = Class.forName(modelConverter);
                if (ModelConverter.class.isAssignableFrom(modelConverterClass)) {
                    final ModelConverter modelConverterInstance = (ModelConverter) modelConverterClass.newInstance();
                    ModelConverters.getInstance().addConverter(modelConverterInstance);
                } else {
                    throw new MojoExecutionException(String.format("Class %s has to be a subclass of %s", modelConverterClass.getName(), ModelConverter.class));
                }
            } catch (final ClassNotFoundException e) {
                throw new MojoExecutionException(String.format("Could not find custom model converter %s", modelConverter), e);
            } catch (final InstantiationException e) {
                throw new MojoExecutionException(String.format("Unable to instantiate custom model converter %s", modelConverter), e);
            } catch (final IllegalAccessException e) {
                throw new MojoExecutionException(String.format("Unable to instantiate custom model converter %s", modelConverter), e);
            }
        }
    }

    public void loadTypesToSkip() throws GenerateException {
        final List<String> typesToSkip = apiSource.getTypesToSkip();
        if (typesToSkip == null) {
            return;
        }
        for (final String typeToSkip : typesToSkip) {
            try {
                final Type type = Class.forName(typeToSkip);
                this.typesToSkip.add(type);
            } catch (final ClassNotFoundException e) {
                throw new GenerateException(e);
            }
        }
    }

    protected File createFile(final File dir, final String outputResourcePath) throws IOException {
        final File serviceFile;
        final int i = outputResourcePath.lastIndexOf("/");
        if (i != -1) {
            final String fileName = outputResourcePath.substring(i + 1);
            final String subDir = outputResourcePath.substring(0, i);
            final File finalDirectory = new File(dir, subDir);
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
            final FileOutputStream fileOutputStream = new FileOutputStream(outputPath);
            final OutputStreamWriter writer = new OutputStreamWriter(fileOutputStream, Charset.forName("UTF-8"));

            final TemplatePath tp = Utils.parseTemplateUrl(templatePath);

            final Handlebars handlebars = new Handlebars(tp.loader);
            initHandlebars(handlebars);

            final Template template = handlebars.compile(tp.name);

            template.apply(swagger, writer);
            writer.close();
            LOG.info("Done!");
        } catch (final MalformedURLException e) {
            throw new GenerateException(e);
        } catch (final IOException e) {
            throw new GenerateException(e);
        }
    }

    private void initHandlebars(final Handlebars handlebars) {
        handlebars.registerHelper("ifeq", new Helper<String>() {
            @Override
            public CharSequence apply(final String value, final Options options) throws IOException {
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
            public CharSequence apply(final String value, final Options options) throws IOException {
                if (value == null) {
                    return null;
                }
                final int lastSlash = value.lastIndexOf("/");
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
     * @return Set<Class<?>> containing all valid classes
     */
    protected Set<Class<?>> getValidClasses() {
        return apiSource.getValidClasses(Api.class);
    }

    /**
     * Resolves all {@link SwaggerExtension} instances configured to be added to the Swagger configuration.
     *
     * @return Collection<SwaggerExtension> which should be added to the swagger configuration
     * @throws GenerateException if the swagger extensions could not be created / resolved
     */
    protected List<SwaggerExtension> resolveSwaggerExtensions() throws GenerateException {
        final List<String> clazzes = apiSource.getSwaggerExtensions();
        final List<SwaggerExtension> resolved = new ArrayList<SwaggerExtension>();
        if (clazzes != null) {
            for (final String clazz : clazzes) {
                final SwaggerExtension extension;
                try {
                    extension = (SwaggerExtension) Class.forName(clazz).newInstance();
                } catch (final Exception e) {
                    throw new GenerateException("Cannot load Swagger extension: " + clazz, e);
                }
                resolved.add(extension);
            }
        }
        return resolved;
    }

    protected ClassSwaggerReader getCustomApiReader(final String customReaderClassName) throws GenerateException {
        try {
            LOG.info("Reading custom API reader: " + customReaderClassName);
            final Class<?> clazz = Class.forName(customReaderClassName);
            if (AbstractReader.class.isAssignableFrom(clazz)) {
                final Constructor<?> constructor = clazz.getConstructor(Swagger.class, Log.class);
                return (ClassSwaggerReader) constructor.newInstance(swagger, LOG);
            } else {
                return (ClassSwaggerReader) clazz.newInstance();
            }
        } catch (final Exception e) {
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

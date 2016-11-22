package com.github.kongchen.swagger.docgen;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.helper.StringHelpers;
import com.github.jknack.handlebars.io.TemplateLoader;
import com.github.kongchen.swagger.docgen.mavenplugin.ApiSource;
import com.github.kongchen.swagger.docgen.reader.AbstractReader;
import com.github.kongchen.swagger.docgen.reader.ClassSwaggerReader;
import com.github.kongchen.swagger.docgen.reader.ModelModifier;
import io.swagger.converter.ModelConverter;
import io.swagger.converter.ModelConverters;
import io.swagger.models.Scheme;
import io.swagger.models.Swagger;
import io.swagger.models.properties.Property;
import io.swagger.util.Json;
import io.swagger.util.Yaml;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import org.apache.maven.plugin.logging.Log;

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
    private ObjectMapper mapper = new ObjectMapper();
    private boolean isSorted = false;

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


    public abstract void loadDocuments() throws GenerateException;
    
    public void toSwaggerDocuments(String uiDocBasePath, String outputFormats, String encoding) throws GenerateException {
        toSwaggerDocuments(uiDocBasePath, outputFormats, null, encoding);
    }

    public void toSwaggerDocuments(String uiDocBasePath, String outputFormats, String fileName, String encoding) throws GenerateException {
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
        cleanupOlds(dir, outputFormats);
        if (fileName == null || "".equals(fileName.trim())) {
            fileName = "swagger";
        }
        try {
            if (outputFormats != null) {
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
            } else {
                // Default to json
                ObjectWriter jsonWriter = mapper.writer(new DefaultPrettyPrinter());
                FileUtils.write(new File(dir, fileName + ".json"), jsonWriter.writeValueAsString(swagger), encoding);
            }
        } catch (IOException e) {
            throw new GenerateException(e);
        }
    }

    public void loadModelModifier() throws GenerateException, IOException {
        ObjectMapper objectMapper = Json.mapper();
        if (apiSource.isUseJAXBAnnotationProcessor()) {
            objectMapper.registerModule(new JaxbAnnotationModule());
            objectMapper.registerModule(new JaxbAnnotationModule());
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
                    throw new MojoExecutionException(
                            String.format("Class %s has to be a subclass of %s",
                                    modelConverterClass.getName(), ModelConverter.class));
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


    private void cleanupOlds(File dir, String outputFormats) {
        if (dir.listFiles() != null && outputFormats != null) {
            for (String format : outputFormats.split(",")) {
                for (File f : dir.listFiles()) {
                    if (f.getName().endsWith(format.toLowerCase())) {
                        f.delete();
                    }
                }
            }
        }
    }

    private void writeInDirectory(File dir, Swagger swaggerDoc,
                                  String basePath) throws GenerateException {

//		try {
//			File serviceFile = createFile(dir, filename);
//			String json = JsonSerializer.asJson(swaggerDoc);
//			JsonNode tree = mapper.readTree(json);
//			if (basePath != null) {
//				((ObjectNode) tree).put("basePath", basePath);
//			}
//
//			JsonUtil.mapper().writerWithDefaultPrettyPrinter()
//					.writeValue(serviceFile, tree);
//		} catch (IOException e) {
//			throw new GenerateException(e);
//		}
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

    private String getUrlParent(URL url) {
        if (url == null) {
            return null;
        }

        String strurl = url.toString();
        int idx = strurl.lastIndexOf('/');
        if (idx == -1) {
            return strurl;
        }
        return strurl.substring(0, idx);
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
    json, yaml
}

class TemplatePath {
    String prefix;
    String name;
    String suffix;
    public TemplateLoader loader;
}

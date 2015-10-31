package com.github.kongchen.swagger.docgen;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.helper.StringHelpers;
import com.github.jknack.handlebars.io.TemplateLoader;
import com.github.kongchen.swagger.docgen.mavenplugin.ApiSource;
import com.github.kongchen.swagger.docgen.reader.ModelModifier;
import io.swagger.converter.ModelConverters;
import io.swagger.models.Scheme;
import io.swagger.models.Swagger;
import io.swagger.models.properties.Property;
import io.swagger.util.Yaml;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 *
 * @author: chekong 05/13/2013
 */
public abstract class AbstractDocumentSource {
    protected final ApiSource apiSource;

    protected final LogAdapter LOG;

    private final String outputPath;

    private final String templatePath;

    private final String swaggerPath;

    private final String modelSubstitute;

    protected final List<Type> typesToSkip = new ArrayList<Type>();

    private final boolean jsonExampleValues;

    protected Swagger swagger;

    private ObjectMapper mapper = new ObjectMapper();
    private boolean isSorted = false;

    protected String swaggerSchemaConverter;

    public AbstractDocumentSource(LogAdapter log, ApiSource apiSource) {
        LOG = log;
        this.outputPath = apiSource.getOutputPath();
        this.templatePath = apiSource.getTemplatePath();
        this.swaggerPath = apiSource.getSwaggerDirectory();
        this.modelSubstitute = apiSource.getModelSubstitute();
        this.jsonExampleValues = apiSource.isJsonExampleValues();

        swagger = new Swagger();
        if (apiSource.getSchemes() != null) {
            if (apiSource.getSchemes().contains(",")) {
                for (String scheme : apiSource.getSchemes().split(",")) {
                    swagger.scheme(Scheme.forValue(scheme));
                }
            } else {
                swagger.scheme(Scheme.forValue(apiSource.getSchemes()));
            }
        }

        swagger.setHost(apiSource.getHost());
        swagger.setInfo(apiSource.getInfo());
        swagger.setBasePath(apiSource.getBasePath());

        this.apiSource = apiSource;
    }


    public abstract void loadDocuments() throws Exception, GenerateException;

    public void toSwaggerDocuments(String uiDocBasePath, String outputFormat) throws GenerateException {
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
        cleanupOlds(dir);

        try {
            if (outputFormat != null && outputFormat.equalsIgnoreCase("yaml")) {
                FileUtils.write(new File(dir, "swagger.yaml"), Yaml.pretty().writeValueAsString(swagger));
            } else {
                ObjectWriter jsonWriter = mapper.writer(new DefaultPrettyPrinter());
                FileUtils.write(new File(dir, "swagger.json"), jsonWriter.writeValueAsString(swagger));
            }
        } catch (IOException e) {
            throw new GenerateException(e);
        }
    }

    public void loadModelModifier() throws GenerateException {

        ModelModifier modelModifier = new ModelModifier(new ObjectMapper());

        List<String> apiModelPropertyAccessExclusions = apiSource.getApiModelPropertyAccessExclusions();
        if (apiModelPropertyAccessExclusions != null && !apiModelPropertyAccessExclusions.isEmpty()) {
            modelModifier.setApiModelPropertyAccessExclusions(apiModelPropertyAccessExclusions);
        }

        if (modelSubstitute != null) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(this.modelSubstitute)));
            String line = null;
            try {
                line = reader.readLine();
                while (line != null) {
                    String[] classes = line.split(":");
                    if (classes == null || classes.length != 2) {
                        throw new GenerateException("Bad format of override model file, it should be ${actualClassName}:${expectClassName}");
                    }
                    modelModifier.addModelSubstitute(classes[0].trim(), classes[1].trim());
                    line = reader.readLine();
                }
            } catch (IOException e) {
                throw new GenerateException(e);
            }
        }

        ModelConverters.getInstance().addConverter(modelModifier);
    }

    public void loadTypesToSkip() throws GenerateException {
        List<String> typesToSkip = apiSource.getTypesToSkip();
        if (typesToSkip != null && !typesToSkip.isEmpty()) {
            for (String typeToSkip : typesToSkip) {
                try {
                    Type type = Class.forName(typeToSkip);
                    this.typesToSkip.add(type);
                } catch (ClassNotFoundException e) {
                    throw new GenerateException(e);
                }
            }
        }
    }


    private void cleanupOlds(File dir) {
        if (dir.listFiles() != null) {
            for (File f : dir.listFiles()) {
                if (f.getName().endsWith("json")) {
                    f.delete();
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

    protected File createFile(File dir, String outputResourcePath)
            throws IOException {
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

        FileOutputStream fileOutputStream;
        try {
            fileOutputStream = new FileOutputStream(outputPath);
        } catch (FileNotFoundException e) {
            throw new GenerateException(e);
        }
        OutputStreamWriter writer = new OutputStreamWriter(fileOutputStream,
                Charset.forName("UTF-8"));

        try {
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
                if (value == null || options.param(0) == null) return options.inverse();
                if (value.equals(options.param(0))) {
                    return options.fn();
                }
                return options.inverse();
            }
        });

        handlebars.registerHelper("basename", new Helper<String>() {
            @Override
            public CharSequence apply(String value, Options options) throws IOException {
                if (value == null) return null;
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

        if (url == null) return null;

        String strurl = url.toString();
        int idx = strurl.lastIndexOf('/');
        if (idx == -1) {
            return strurl;
        }
        return strurl.substring(0, idx);
    }


}


class TemplatePath {
    String prefix;
    String name;
    String suffix;
    public TemplateLoader loader;
}

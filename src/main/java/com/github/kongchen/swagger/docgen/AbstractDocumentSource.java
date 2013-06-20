package com.github.kongchen.swagger.docgen;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.kongchen.swagger.docgen.mustache.OutputTemplate;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.wordnik.swagger.core.Documentation;

/**
 * Created with IntelliJ IDEA.
 *
 * @author: chekong
 * 05/13/2013
 */
public abstract class AbstractDocumentSource {
    private final String outputPath;

    private final String templatePath;

    private final String swaggerPath;

    protected final LogAdapter LOG;

    private String basePath;

    private String apiVersion;

    private ObjectMapper mapper = new ObjectMapper();

    private OutputTemplate outputTemplate;

    public AbstractDocumentSource(LogAdapter logAdapter, String outputPath, String outputTpl, String swaggerOutput) {
        LOG = logAdapter;
        this.outputPath = outputPath;
        this.templatePath = outputTpl;
        this.swaggerPath = swaggerOutput;
    }

    public abstract void loadDocuments() throws Exception, GenerateException;

    protected Documentation serviceDocument;

    List<Documentation> validDocuments = new LinkedList<Documentation>();

    public String getBasePath() {
        return basePath;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public OutputTemplate getOutputTemplate() {
        return outputTemplate;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    protected void acceptDocument(Documentation doc) {
        validDocuments.add(doc);
    }

    public List<Documentation> getValidDocuments() {
        return validDocuments;
    }

    public void toSwaggerDocuments() throws GenerateException {
        if (swaggerPath == null) {
            return;
        }
        File dir = new File(swaggerPath);
        if (dir.isFile()) {
            throw new GenerateException(
                    String.format("Swagger-outputDirectory[%s] must be a directory!", swaggerPath));
        }

        if (!dir.exists()) {
            try {
                FileUtils.forceMkdir(dir);
            } catch (IOException e) {
                throw new GenerateException(
                        String.format("Create Swagger-outputDirectory[%s] failed.", swaggerPath));
            }
        }
        cleanupOlds(dir);

        writeInDirectory(dir, serviceDocument);
        for (Documentation doc : validDocuments) {
            writeInDirectory(dir, doc);
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

    private String resourcePathToFilename(String resourcePath) {
        if (resourcePath == null) {
            return "service.json";
        }
        String name = resourcePath;
        if (resourcePath.startsWith("/")) {
            name = resourcePath.substring(1);
        }
        name = name.replaceAll("/", "_");

        return name + ".json";
    }

    private void writeInDirectory(File dir, Documentation doc) throws GenerateException {
        String filename = resourcePathToFilename(doc.getResourcePath());
        File serviceFile = new File(dir, filename);
        try {
            while (!serviceFile.createNewFile()) {
                serviceFile.delete();
            }
            mapper.writerWithDefaultPrettyPrinter().writeValue(serviceFile, doc);
        } catch (IOException e) {
            throw new GenerateException(e);
        }
    }

    public OutputTemplate prepareMustacheTemplate() throws Exception {
        this.outputTemplate = new OutputTemplate(this);
        return outputTemplate;

    }
    public void toDocuments() throws Exception {
        if (outputTemplate == null) {
            prepareMustacheTemplate();
        }
        if (outputTemplate.getApiDocuments().isEmpty()) {
            LOG.warn("nothing to write.");
            return;
        }
        LOG.info("Writing doc to " + outputPath + "...");

        FileOutputStream fileOutputStream = new FileOutputStream(outputPath);
        OutputStreamWriter writer = new OutputStreamWriter(fileOutputStream, Charset.forName("UTF-8"));
        MustacheFactory mf = new DefaultMustacheFactory();
        Mustache mustache = mf.compile(templatePath);
        mustache.execute(writer, outputTemplate).flush();
        writer.close();
        LOG.info("Done!");
    }
}

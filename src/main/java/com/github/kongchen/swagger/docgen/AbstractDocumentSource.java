package com.github.kongchen.swagger.docgen;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
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
    protected final LogAdapter LOG;

    private final String outputPath;

    private final String templatePath;

    private final String swaggerPath;

    protected Documentation serviceDocument;

    List<Documentation> validDocuments = new LinkedList<Documentation>();

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

    public String getBasePath() {
        return basePath;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    public OutputTemplate getOutputTemplate() {
        return outputTemplate;
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

    public OutputTemplate prepareMustacheTemplate() {
        this.outputTemplate = new OutputTemplate(this);
        return outputTemplate;

    }

    public void toDocuments() throws GenerateException {
        if (outputTemplate == null) {
            prepareMustacheTemplate();
        }
        if (outputTemplate.getApiDocuments().isEmpty()) {
            LOG.warn("nothing to write.");
            return;
        }
        LOG.info("Writing doc to " + outputPath + "...");

        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(outputPath);
        } catch (FileNotFoundException e) {
            throw new GenerateException(e);
        }
        OutputStreamWriter writer = new OutputStreamWriter(fileOutputStream, Charset.forName("UTF-8"));
        MustacheFactory mf = new DefaultMustacheFactory();


        URI uri = null;
        try {
            uri = new URI(templatePath);
        } catch (URISyntaxException e) {
            throw new GenerateException(e);
        }
        if (!uri.isAbsolute()) {
            File file = new File(templatePath);
            if (!file.exists()) {
                throw new GenerateException("Template " + file.getAbsoluteFile()
                        + " not found. You can go to https://github.com/kongchen/api-doc-template to get templates.");
            } else {
                uri = new File(templatePath).toURI();
            }
        }

        URL url = null;
        try {
            url = uri.toURL();
            InputStreamReader reader = new InputStreamReader(url.openStream(), Charset.forName("UTF-8"));
            Mustache mustache = mf.compile(reader, templatePath);

            mustache.execute(writer, outputTemplate).flush();
            writer.close();
            LOG.info("Done!");
        } catch (MalformedURLException e) {
            throw new GenerateException(e);
        } catch (IOException e) {
            throw new GenerateException(e);
        }
    }
}

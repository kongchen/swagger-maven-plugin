package com.github.kongchen.swagger.docgen;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
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
import com.wordnik.swagger.core.Documentation;

/**
 * Created with IntelliJ IDEA.
 *
 * @author: chekong 05/13/2013
 */
public abstract class AbstractDocumentSource {

    protected final LogAdapter LOG;

    private final String outputPath;

    private final String templatePath;

    private final String mustacheFileRoot;

    private final String swaggerPath;

    protected Documentation serviceDocument;

    List<Documentation> validDocuments = new LinkedList<Documentation>();

    private String basePath;

    private String apiVersion;

    private ObjectMapper mapper = new ObjectMapper();

    private OutputTemplate outputTemplate;

    private boolean useOutputFlatStructure;

    public AbstractDocumentSource(LogAdapter logAdapter, String outputPath, String outputTpl, String swaggerOutput, String mustacheFileRoot, boolean useOutputFlatStructure1) {
        LOG = logAdapter;
        this.outputPath = outputPath;
        this.templatePath = outputTpl;
        this.mustacheFileRoot = mustacheFileRoot;
        this.useOutputFlatStructure = useOutputFlatStructure1;
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

        if (useOutputFlatStructure) {
            name = name.replaceAll("/", "_");
        }

        return name + ".json";
    }

    private void writeInDirectory(File dir, Documentation doc) throws GenerateException {
        String filename = resourcePathToFilename(doc.getResourcePath());
        try {
            File serviceFile = createFile(dir, filename);
            mapper.writerWithDefaultPrettyPrinter().writeValue(serviceFile, doc);
        } catch (IOException e) {
            throw new GenerateException(e);
        }
    }

    private File createFile(File dir, String outputResourcePath) throws IOException {
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

        try {
            URL url = getTemplateUri().toURL();
            InputStreamReader reader = new InputStreamReader(url.openStream(), Charset.forName("UTF-8"));
            Mustache mustache = getMustacheFactory().compile(reader, templatePath);

            mustache.execute(writer, outputTemplate).flush();
            writer.close();
            LOG.info("Done!");
        } catch (MalformedURLException e) {
            throw new GenerateException(e);
        } catch (IOException e) {
            throw new GenerateException(e);
        }
    }

    private URI getTemplateUri() throws GenerateException {
        URI uri = null;
        try {
            uri = new URI(templatePath);
        } catch (URISyntaxException e) {
            File file = new File(templatePath);
            if (!file.exists()) {
                throw new GenerateException("Template " + file.getAbsoluteFile()
                        + " not found. You can go to https://github.com/kongchen/api-doc-template to get templates.");
            }
            uri = file.toURI();
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
        return uri;
    }

    private DefaultMustacheFactory getMustacheFactory() {
        if (mustacheFileRoot == null) {
            return new DefaultMustacheFactory();
        } else {
            return new DefaultMustacheFactory(new File(mustacheFileRoot));
        }
    }
}

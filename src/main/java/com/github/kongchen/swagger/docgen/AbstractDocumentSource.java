package com.github.kongchen.swagger.docgen;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wordnik.swagger.core.Documentation;

/**
 * Created with IntelliJ IDEA.
 *
 * @author: chekong
 * 05/13/2013
 */
public abstract class AbstractDocumentSource {
    private String basePath;

    private String apiVersion;

    private ObjectMapper mapper = new ObjectMapper();

    public abstract void documentsIn() throws Exception, GenerateException;

    protected Documentation serviceDocument;

    List<Documentation> validDocuments = new LinkedList<Documentation>();

    public String getBasePath() {
        return basePath;
    }

    public String getApiVersion() {
        return apiVersion;
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

    public void writeSwaggerDocuments(String outputDirectory) throws GenerateException {
        if (outputDirectory == null) {
            return;
        }
        File dir = new File(outputDirectory);
        if (dir.isFile()) {
            throw new GenerateException(
                    String.format("Swagger-outputDirectory[%s] must be a directory!", outputDirectory));
        }

        if (!dir.exists()) {
            try {
                FileUtils.forceMkdir(dir);
            } catch (IOException e) {
                throw new GenerateException(
                        String.format("Create Swagger-outputDirectory[%s] failed.", outputDirectory));
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
}

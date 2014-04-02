package com.github.kongchen.swagger.docgen.mavenplugin;

import com.github.kongchen.swagger.docgen.AbstractDocumentSource;
import com.github.kongchen.swagger.docgen.ApiSource;
import com.github.kongchen.swagger.docgen.GenerateException;
import com.github.kongchen.swagger.docgen.LogAdapter;
import com.github.kongchen.swagger.docgen.SwaggerDocumentSource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import java.io.File;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: kongchen
 * Date: 3/7/13
 */

/**
 * @goal generate
 * @phase compile
 * @configurator include-project-dependencies
 * @requiresDependencyResolution runtime
 */
public class ApiDocumentMojo extends AbstractMojo {

    /**
     * @parameter
     */
    private List<ApiSource> apiSources;

    public List<ApiSource> getApiSources() {
        return apiSources;
    }

    public void setApiSources(List<ApiSource> apiSources) {
        this.apiSources = apiSources;
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (apiSources == null) {
            throw new MojoFailureException("You must configure at least one apiSources element");
        }
        try {
            Class<?> tryClass = Class.forName("com.wordnik.swagger.annotations.ApiErrors");
            throw new MojoExecutionException("You may use an old version of swagger which is not supported by swagger-maven-plugin 2.0+\n" +
                    "swagger-maven-plugin 2.0+ only supports swagger-core 1.3.x");
        } catch (ClassNotFoundException e) {
            //ignore
        }

        try {
            getLog().debug(apiSources.toString());
            for (ApiSource apiSource : apiSources) {
                File outputDirectory = new File(apiSource.getOutputPath()).getParentFile();
                if (outputDirectory != null && !outputDirectory.exists()) {
                    if (!outputDirectory.mkdirs()) {
                        throw new MojoExecutionException("Create directory[" +
                                apiSource.getOutputPath() + "] for output failed.");
                    }
                }
                AbstractDocumentSource documentSource = new SwaggerDocumentSource(apiSource, new LogAdapterImpl (getLog()));
                documentSource.loadDocuments();
                documentSource.toDocuments();
                documentSource.toSwaggerDocuments(apiSource.getBasePath());
            }

        } catch (GenerateException e) {
            throw new MojoFailureException(e.getMessage(), e);
        } catch (Exception e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }
}

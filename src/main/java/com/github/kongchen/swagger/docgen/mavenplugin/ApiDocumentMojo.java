package com.github.kongchen.swagger.docgen.mavenplugin;

import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import com.github.kongchen.swagger.docgen.AbstractDocumentSource;
import com.github.kongchen.swagger.docgen.GenerateException;

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
        try {
            getLog().debug(apiSources.toString());
            for (ApiSource apiSource : apiSources) {
                AbstractDocumentSource documentSource = new MavenDocumentSource(apiSource, getLog());
                documentSource.loadDocuments();
                documentSource.toDocuments();
                documentSource.toSwaggerDocuments();
            }

        } catch (Exception e) {
            throw new MojoFailureException(e.getMessage());
        } catch (GenerateException e) {
            throw new MojoFailureException(e.getMessage());
        }
    }
}

package com.github.kongchen.swagger.docgen.mavenplugin;

import java.io.File;
import java.util.List;

import com.github.kongchen.swagger.docgen.AbstractDocumentSource;
import com.github.kongchen.swagger.docgen.GenerateException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

/**
 * Created with IntelliJ IDEA.
 * User: kongchen
 * Date: 3/7/13
 */
@Mojo( name = "generate", defaultPhase = LifecyclePhase.COMPILE, configurator = "include-project-dependencies", 
       requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class ApiDocumentMojo extends AbstractMojo {

    /**
     * A set of apiSources.
     * One apiSource can be considered as a set of APIs for one apiVersion in a basePath
     * 
     */
    @Parameter
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
        if (useSwaggerSpec11()) {
            throw new MojoExecutionException("You may use an old version of swagger which is not supported by swagger-maven-plugin 2.0+\n" +
                "swagger-maven-plugin 2.0+ only supports swagger-core 1.3.x");
        }

        try {
            getLog().debug(apiSources.toString());
            for (ApiSource apiSource : apiSources) {

                AbstractDocumentSource documentSource;

                if(apiSource.isSupportSpringMvc()){
                	documentSource = new SpringMavenDocumentSource(apiSource, getLog());
                }else{
                	documentSource = new MavenDocumentSource(apiSource, getLog());
                }
                
                documentSource.loadOverridingModels();
                documentSource.loadDocuments();
				if (apiSource.getOutputPath() != null){
					File outputDirectory = new File(apiSource.getOutputPath()).getParentFile();
					if (outputDirectory != null && !outputDirectory.exists()) {
						if (!outputDirectory.mkdirs()) {
							throw new MojoExecutionException("Create directory[" +
									apiSource.getOutputPath() + "] for output failed.");
						}
					}
				}
				if (apiSource.getOutputTemplate()!=null)
				{
					documentSource.toDocuments();
				}
                documentSource.toSwaggerDocuments(
                        apiSource.getSwaggerUIDocBasePath() == null
                                ? apiSource.getBasePath()
                                : apiSource.getSwaggerUIDocBasePath());
            }

        } catch (GenerateException e) {
            throw new MojoFailureException(e.getMessage(), e);
        } catch (Exception e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

    private boolean useSwaggerSpec11() {
        try {
            Class<?> tryClass = Class.forName("com.wordnik.swagger.annotations.ApiErrors");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}

package com.github.kongchen.swagger.docgen.mavenplugin;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.reflections.Reflections;

import com.github.kongchen.swagger.docgen.AbstractDocumentSource;
import com.github.kongchen.swagger.docgen.GenerateException;
import com.github.kongchen.swagger.docgen.mustache.OutputTemplate;
import com.github.kongchen.swagger.docgen.mustache.TemplateOutputWriter;
import com.wordnik.swagger.annotations.Api;

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
    private String apiSources;

    /**
     * @parameter
     */
    private String apiVersion;

    /**
     * @parameter
     */
    private String basePath;

    /**
     * @parameter
     */
    private String outputTemplate;

    /**
     * @parameter
     */
    private String outputPath;

    /**
     * @parameter
     */
    private String swaggerDirectory;

    public Set<Class> getValidClasses() throws GenerateException {
        Set<Class> classes = new HashSet<Class>();
        if (getApiSources() == null) {
            Set<Class<?>> c = new Reflections().getTypesAnnotatedWith(Api.class);
            classes.addAll(c);
        } else {
            if (apiSources.contains(";")) {
                String[] sources = apiSources.split(";");
                for (String source : sources) {
                    Set<Class<?>> c = new Reflections(source).getTypesAnnotatedWith(Api.class);
                    classes.addAll(c);
                }
            }else {
                classes.addAll(new Reflections(apiSources).getTypesAnnotatedWith(Api.class));
            }
        }
        Iterator<Class> it = classes.iterator();
        while (it.hasNext()) {
            if (it.next().getName().startsWith("com.wordnik.swagger")){
                it.remove();
            }
        }
        getLog().info("Inputs are:" + classes.toString());
        return classes;
    }

    public String getApiSources() {
        return apiSources;
    }

    public void setApiSources(String apiSources) {
        this.apiSources = apiSources;
    }

    public String getOutputTemplate() {
        return outputTemplate;
    }

    public void setOutputTemplate(String outputTemplate) {
        this.outputTemplate = outputTemplate;
    }

    public String getOutputPath() {
        return outputPath;
    }

    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    public String getBasePath() {
        return basePath;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    public String getSwaggerDirectory() {
        return swaggerDirectory;
    }

    public void setSwaggerDirectory(String swaggerDirectory) {
        this.swaggerDirectory = swaggerDirectory;
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            TemplateOutputWriter templateOutputWriter = new TemplateOutputWriter(getOutputTemplate(),
                    getOutputPath());
            AbstractDocumentSource docSource = new MavenDocumentSource(this, getLog());
            docSource.documentsIn();
            templateOutputWriter.writeBy(new OutputTemplate(docSource));
            docSource.writeSwaggerDocuments(getSwaggerDirectory());
        } catch (Exception e) {
            throw new MojoFailureException(e.getMessage());
        } catch (GenerateException e) {
            throw new MojoFailureException(e.getMessage());
        }
    }
}

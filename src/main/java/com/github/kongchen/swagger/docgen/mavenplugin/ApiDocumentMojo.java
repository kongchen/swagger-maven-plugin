package com.github.kongchen.swagger.docgen.mavenplugin;

import com.github.kongchen.swagger.docgen.AbstractDocumentSource;
import com.github.kongchen.swagger.docgen.mustache.OutputTemplate;
import com.github.kongchen.swagger.docgen.mustache.TemplateOutputWriter;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * Created with IntelliJ IDEA.
 *
 * @author: chekong
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

    private Set<String> outputSet = new HashSet<String>();

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            for (ApiSource apiSource : apiSources) {
                TemplateOutputWriter templateOutputWriter = new TemplateOutputWriter(apiSource.getOutputTemplate(),
                        apiSource.getOutputPath());
                AbstractDocumentSource docSource = new MavenDocumentSource(apiSource, getLog());
                // check duplicate output path
                if (!outputSet.add(new File(templateOutputWriter.getDocumentOutputPath()).getAbsolutePath())) {
                    throw new MojoExecutionException("output path " + templateOutputWriter.getDocumentOutputPath()
                            + " is specified to more than one api");
                }

                templateOutputWriter.writeBy(new OutputTemplate(docSource));

            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new MojoFailureException(e.getMessage());
        }
    }
}

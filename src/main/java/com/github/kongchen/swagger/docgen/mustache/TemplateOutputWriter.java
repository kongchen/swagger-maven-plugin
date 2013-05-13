package com.github.kongchen.swagger.docgen.mustache;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import org.slf4j.LoggerFactory;

import com.github.kongchen.swagger.docgen.LogAdapter;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

/**
 * Created with IntelliJ IDEA.
 *
 * @author: chekong
 * 05/13/2013
 */
public class TemplateOutputWriter {
    private final LogAdapter LOG;

    String outputTemplatePath;

    String documentOutputPath;

    public TemplateOutputWriter(String outputTemplatePath, String documentOutputPath) {
        LOG = new LogAdapter(LoggerFactory.getLogger(TemplateOutputWriter.class));
        this.documentOutputPath = documentOutputPath;
        this.outputTemplatePath = outputTemplatePath;
    }

    public String getOutputTemplatePath() {
        return outputTemplatePath;
    }

    public String getDocumentOutputPath() {
        return documentOutputPath;
    }

    /**
     * Write output file in documentOutputPath by the outputTemplate
     * @param outputTemplate
     * @throws IOException
     */
    public void writeBy(OutputTemplate outputTemplate) throws IOException {
        if (outputTemplate.getApiDocuments().isEmpty()) {
            LOG.warn("nothing to write.");
            return;
        }
        LOG.info("Writing doc to " + getDocumentOutputPath() + "...");

        Writer writer = new FileWriter(getDocumentOutputPath());
        MustacheFactory mf = new DefaultMustacheFactory();
        Mustache mustache = mf.compile(getOutputTemplatePath());
        mustache.execute(writer, outputTemplate).flush();
        writer.close();
        LOG.info("Done!");
    }
}

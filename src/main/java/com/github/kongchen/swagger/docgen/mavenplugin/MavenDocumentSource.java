package com.github.kongchen.swagger.docgen.mavenplugin;

import org.apache.maven.plugin.logging.Log;

import com.github.kongchen.swagger.docgen.AbstractDocumentSource;
import com.github.kongchen.swagger.docgen.GenerateException;
import com.github.kongchen.swagger.docgen.LogAdapter;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.core.Documentation;
import com.wordnik.swagger.core.DocumentationEndPoint;
import com.wordnik.swagger.core.SwaggerSpec;
import com.wordnik.swagger.jaxrs.HelpApi;
import com.wordnik.swagger.jaxrs.JaxrsApiSpecParser;

/**
 * Created with IntelliJ IDEA.
 *
 * @author: chekong
 * 05/13/2013
 */
public class MavenDocumentSource extends AbstractDocumentSource {
    private final ApiSource apiSource;

    public MavenDocumentSource(ApiSource apiSource, Log log) {
        super(new LogAdapter(log),
                apiSource.getOutputPath(), apiSource.getOutputTemplate(), apiSource.getSwaggerDirectory());

        setApiVersion(apiSource.getApiVersion());
        setBasePath(apiSource.getBasePath());
        this.apiSource = apiSource;
    }

    @Override
    public void loadDocuments() throws GenerateException {
        serviceDocument = new Documentation(apiSource.getApiVersion(), SwaggerSpec.version(),
                apiSource.getBasePath(), null);
        for (Class c : apiSource.getValidClasses()) {
            Documentation doc = null;
            try {
                doc = getDocFromClass(c, getApiVersion(), getBasePath());
            } catch (Exception e) {
                throw new GenerateException(e);
            }
            if (doc == null) continue;
            LOG.info("Detect Resource:" + c.getName());
            serviceDocument.addApi(new DocumentationEndPoint(doc.getResourcePath(), ""));
            acceptDocument(doc);
        }
    }

    private Documentation getDocFromClass(Class c, String apiVersion, String basePath) throws Exception {
        Api resource = (Api) c.getAnnotation(Api.class);

        if (resource == null) return null;
        JaxrsApiSpecParser parser = new JaxrsApiSpecParser(c, apiVersion,
                SwaggerSpec.version(), basePath, resource.value());

        return new HelpApi().filterDocs(parser.parse(), null, null, null, null);
    }
}

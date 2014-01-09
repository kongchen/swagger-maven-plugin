package com.github.kongchen.swagger.docgen.mavenplugin;

import com.github.kongchen.swagger.docgen.AbstractDocumentSource;
import com.github.kongchen.swagger.docgen.GenerateException;
import com.github.kongchen.swagger.docgen.LogAdapter;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.core.Documentation;
import com.wordnik.swagger.core.DocumentationEndPoint;
import com.wordnik.swagger.core.SwaggerSpec;
import com.wordnik.swagger.jaxrs.HelpApi;
import com.wordnik.swagger.jaxrs.JaxrsApiSpecParser;
import org.apache.maven.plugin.logging.Log;

import java.util.Map;
import java.util.TreeMap;

import static java.util.AbstractMap.SimpleEntry;

/**
 * Created with IntelliJ IDEA.
 *
 * @author: chekong
 * 05/13/2013
 */
public class MavenDocumentSource extends AbstractDocumentSource {
    private final ApiSource apiSource;

    private Map<String, Documentation> docMap = new TreeMap<String, Documentation>();

    public MavenDocumentSource(ApiSource apiSource, Log log) {
        super(new LogAdapter(log),
                apiSource.getOutputPath(), apiSource.getOutputTemplate(), apiSource.getSwaggerDirectory(), apiSource.mustacheFileRoot, apiSource.isUseOutputFlatStructure());

        setApiVersion(apiSource.getApiVersion());
        setBasePath(apiSource.getBasePath());
        this.apiSource = apiSource;
    }

    @Override
    public void loadDocuments() throws GenerateException {
        serviceDocument = new Documentation(apiSource.getApiVersion(), SwaggerSpec.version(),
                apiSource.getBasePath(), null);
        for (Class c : apiSource.getValidClasses()) {
            SimpleEntry<Documentation, Api> entry;
            try {
                entry = getDocFromClass(c, getApiVersion(), getBasePath());
            } catch (Exception e) {
                throw new GenerateException(e);
            }
            if (entry == null) continue;
            LOG.info("Detect Resource:" + c.getName());

            Documentation doc = entry.getKey();
            Api resource = entry.getValue();

            serviceDocument.addApi(new DocumentationEndPoint(doc.getResourcePath(), resource.description()));
            docMap.put(doc.getResourcePath(), doc);
        }
        // to keep order
        for (Documentation doc : docMap.values()) {
            if (!apiSource.isWithFormatSuffix()) {
                for (DocumentationEndPoint endPoint : doc.getApis()) {
                    endPoint.setPath(endPoint.getPath().replaceAll("\\.\\{format\\}",""));
                }
            }
            acceptDocument(doc);
        }
    }

    private SimpleEntry<Documentation, Api> getDocFromClass(Class c, String apiVersion, String basePath) throws Exception {
        Api resource = (Api) c.getAnnotation(Api.class);

        if (resource == null) return null;
        JaxrsApiSpecParser parser = new JaxrsApiSpecParser(c, apiVersion,
                SwaggerSpec.version(), basePath, resource.value());

        Documentation doc = new HelpApi().filterDocs(parser.parse(), null, null, null, null);
        if (doc == null) return null;

        return new SimpleEntry<Documentation, Api>(doc, resource);
    }
}

package com.github.kongchen.swagger.docgen.remote;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.kongchen.swagger.docgen.AbstractDocumentSource;
import com.github.kongchen.swagger.docgen.LogAdapter;
import com.wordnik.swagger.core.Documentation;
import com.wordnik.swagger.core.DocumentationEndPoint;

/**
 * Created with IntelliJ IDEA.
 *
 * @author: chekong
 * 05/13/2013
 */
public class RemoteDocumentSource extends AbstractDocumentSource {
    private final LogAdapter LOG;

    private final URI requestURI;

    ObjectMapper mapper = new ObjectMapper();

    private boolean withFormatSuffix = true;

    public RemoteDocumentSource(LogAdapter logAdapter, URI requestURI, String outputTpl, String outputPath, String swaggerOutput) {
        super(logAdapter, outputPath, outputTpl, swaggerOutput);
        LOG = new LogAdapter(Logger.getLogger(RemoteDocumentSource.class));
        this.requestURI = requestURI;
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_DEFAULT);
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public void loadDocuments() throws IOException {
        HttpClient client = new DefaultHttpClient();
        HttpResponse response = client.execute(new HttpGet(requestURI));

        Documentation doc = mapper.readValue(response.getEntity().getContent(), Documentation.class);
        serviceDocument = doc;

        setApiVersion(doc.getApiVersion());
        setBasePath(doc.getBasePath());
        URIBuilder uriBuilder = new URIBuilder(requestURI);
        String path = uriBuilder.getPath();

        for (DocumentationEndPoint endPoint : doc.getApis()) {

            String _endpoint = endPoint.getPath().replaceAll("/api-docs\\.\\{format\\}", "");
            uriBuilder.setPath((path + "/" + _endpoint).replaceAll("\\/\\/", "/"));
            String newURL = null;
            try {
                newURL = uriBuilder.build().toString();
            } catch (URISyntaxException e) {
                LOG.error("URL " + newURL + "is not valid.");
                continue;
            }
            LOG.info("calling " + newURL);
            response = client.execute(new HttpGet(newURL));
            Documentation _doc = mapper.readValue(response.getEntity().getContent(), Documentation.class);

            if (!withFormatSuffix) {
                for (DocumentationEndPoint ep : _doc.getApis()) {
                    ep.setPath(ep.getPath().replaceAll("\\.\\{format}",""));
                }
            }
            acceptDocument(_doc);
        }
    }

    public void withFormatSuffix(boolean with) {
        this.withFormatSuffix = with;
    }
}

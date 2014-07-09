package com.github.kongchen.swagger.docgen.remote;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.kongchen.swagger.docgen.AbstractDocumentSource;
import com.github.kongchen.swagger.docgen.LogAdapter;
import com.wordnik.swagger.model.ApiDescription;
import com.wordnik.swagger.model.ApiListing;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;

import scala.collection.Iterator;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

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

    public RemoteDocumentSource(LogAdapter logAdapter, URI requestURI, String outputTpl, String outputPath, String swaggerOutput, String mustacheFileRoot, boolean useOutputFlatStructure, String overridingModels) {
        super(logAdapter, outputPath, outputTpl, swaggerOutput, mustacheFileRoot, useOutputFlatStructure, overridingModels);
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

        if (response.getStatusLine().getStatusCode() != 200) {
            throw new IOException(requestURI + " got " + response.getStatusLine().getReasonPhrase());
        }
        ApiListing doc = mapper.readValue(response.getEntity().getContent(), ApiListing.class);
//        serviceDocument = doc;

        setApiVersion(doc.apiVersion());
        setBasePath(doc.basePath());
        URIBuilder uriBuilder = new URIBuilder(requestURI);
        String path = uriBuilder.getPath();

        for (Iterator<ApiDescription> iterator = doc.apis().iterator(); iterator.hasNext(); ) {
            ApiDescription endPoint = iterator.next();

            String _endpoint = endPoint.path().replaceAll("/api-docs\\.\\{format\\}", "");
            uriBuilder.setPath((path + "/" + _endpoint).replaceAll("\\/\\/", "/"));
            String newURL = null;
            try {
                newURL = uriBuilder.build().toString();
            } catch (URISyntaxException e) {
                LOG.error("URL is not valid." + e);
                continue;
            }
            LOG.info("calling " + newURL);
            response = client.execute(new HttpGet(newURL));
            ApiListing _doc = mapper.readValue(response.getEntity().getContent(), ApiListing.class);

            if (!withFormatSuffix) {
                for (Iterator<ApiDescription> iterator1 = _doc.apis().iterator(); iterator1.hasNext(); ) {
                    ApiDescription ep = iterator1.next();
//                    ep.path() = (ep.path().replaceAll("\\.\\{format}", ""));
                }
            }
            acceptDocument(_doc);
        }
    }

    public void withFormatSuffix(boolean with) {
        this.withFormatSuffix = with;
    }
}

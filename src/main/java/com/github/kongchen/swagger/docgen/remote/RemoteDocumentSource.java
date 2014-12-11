package com.github.kongchen.swagger.docgen.remote;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.kongchen.swagger.docgen.AbstractDocumentSource;
import com.github.kongchen.swagger.docgen.LogAdapter;
import com.github.kongchen.swagger.docgen.remote.model.*;
import com.github.kongchen.swagger.docgen.util.Utils;
import com.wordnik.swagger.model.ApiListing;
import com.wordnik.swagger.model.ApiListingReference;
import com.wordnik.swagger.model.AuthorizationType;
import com.wordnik.swagger.model.ResourceListing;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;

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

    public RemoteDocumentSource(LogAdapter logAdapter, URI requestURI, String outputTpl, String outputPath,
                                String swaggerOutput, String mustacheFileRoot, boolean useOutputFlatStructure,
                                String overridingModels, String apiComparator) {
        super(logAdapter, outputPath, outputTpl, swaggerOutput, mustacheFileRoot, useOutputFlatStructure, overridingModels, apiComparator);
        LOG = new LogAdapter(Logger.getLogger(RemoteDocumentSource.class));
        this.requestURI = requestURI;
        mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
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

        JResourceListing doc = mapper.readValue(response.getEntity().getContent(), JResourceListing.class);


        setApiVersion(doc.getApiVersion());
//        setBasePath(doc.getBasePath());
        URIBuilder uriBuilder = new URIBuilder(requestURI);
        String path = uriBuilder.getPath();
        for (JApiListingReference endPoint : doc.getApis()) {
            String _endpoint = endPoint.getPath().replaceAll("/api-docs\\.\\{format\\}", "");
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
            JApiListing _doc = mapper.readValue(response.getEntity().getContent(), JApiListing.class);

            if (!withFormatSuffix) {
                for (JApiDescription ep : _doc.getApis()) {
                    ep.setPath(ep.getPath().replaceAll("\\.\\{format}", ""));
                }
            }
            ApiListing apiListing = _doc.toSwaggerModel();
            acceptDocument(apiListing);

        }

        serviceDocument = new ResourceListing(doc.getApiVersion(), doc.getSwaggerVersion(),
                new ListConverter<JApiListingReference, ApiListingReference>(doc.getApis()).convert(),
                new ListConverter<JAuthorizationType, AuthorizationType>(doc.getAuthorizations()).convert(),
                Utils.getOption(doc.getInfo().toSwaggerModel()));
    }

    public void withFormatSuffix(boolean with) {
        this.withFormatSuffix = with;
    }

}


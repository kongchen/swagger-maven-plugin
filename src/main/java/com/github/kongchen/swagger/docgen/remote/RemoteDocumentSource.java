package com.github.kongchen.swagger.docgen.remote;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;

import com.github.kongchen.swagger.docgen.AbstractDocumentSource;
import com.github.kongchen.swagger.docgen.LogAdapter;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.DefaultHttpClient;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
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

    public RemoteDocumentSource(URI requestURI) {
        LOG = new LogAdapter(LoggerFactory.getLogger(RemoteDocumentSource.class));
        this.requestURI = requestURI;
    }

    public List<Documentation> toSwaggerDocuments() throws IOException {
        HttpClient client = new DefaultHttpClient();
        HttpResponse response = client.execute(new HttpGet(requestURI));
        ObjectMapper mapper = new ObjectMapper();
        Documentation doc = mapper.readValue(response.getEntity().getContent(), Documentation.class);

        setApiVersion(doc.getApiVersion());
        setBasePath(doc.getBasePath());
        URIBuilder uriBuilder = new URIBuilder(requestURI);
        String path = uriBuilder.getPath();

        List<Documentation> validDocuments = new LinkedList<Documentation>();
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
            validDocuments.add(_doc);
        }

        return validDocuments;
    }
}

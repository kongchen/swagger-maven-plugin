package com.github.kongchen.mavenswagger.apigen;


import com.github.kongchen.mavenswagger.apigen.output.MformatApiDocument;
import com.github.kongchen.mavenswagger.apigen.output.OutputTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.wordnik.swagger.core.Documentation;
import com.wordnik.swagger.core.DocumentationEndPoint;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.maven.plugin.logging.Log;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Method;
import java.util.Collection;

/**
 * Created with IntelliJ IDEA.
 *
 * @author: chekong
 */
public class ApiDocumentGenerator {
    private final LogAdapter LOG;

    private OutputTemplate outputTemplate = new OutputTemplate();

    public ApiDocumentGenerator() {
        LOG = new LogAdapter(LoggerFactory.getLogger(ApiDocumentGenerator.class));

    }

    public ApiDocumentGenerator(Log log) {
        LOG = new LogAdapter(log);
    }

    public static void main(String args[]) throws Exception {
        String outputTpl = "markdown.mustache";
        String outputPath = "apidoc.md";
        String requestURL = "http://petstore.swagger.wordnik.com/api/api-docs.json?api_key=special-key";

        if (args.length != 3) {
            System.out.println("args: swagger-doc-url output-template-path output-file-path");
            System.out.println("\ttemplate json-schema:\n\t\t" + OutputTemplate.getJsonSchema());
            return;
        }
        requestURL = args[0];
        outputTpl = args[1];
        outputPath = args[2];

        ApiDocumentGenerator apiGenerator = new ApiDocumentGenerator();
        apiGenerator.addDocsFromURL(requestURL);
        apiGenerator.generateDoc(outputTpl, outputPath);
    }

    public void addDocsFromURL(String requestURL) throws Exception {
        HttpClient client = new DefaultHttpClient();
        HttpResponse response = client.execute(new HttpGet(requestURL));
        ObjectMapper mapper = new ObjectMapper();
        Documentation doc = mapper.readValue(response.getEntity().getContent(), Documentation.class);

        outputTemplate.setApiVersion(doc.getApiVersion());
        outputTemplate.setBasePath(doc.getBasePath());
        URIBuilder uriBuilder = new URIBuilder(requestURL);
        String path = uriBuilder.getPath();

        for (DocumentationEndPoint endPoint : doc.getApis()) {
            String _endpoint = endPoint.getPath().replaceAll("/api-docs\\.\\{format\\}", "");
            uriBuilder.setPath(path + _endpoint);
            String newURL = uriBuilder.build().toString();
            LOG.info("calling " + newURL);
            response = client.execute(new HttpGet(newURL));
            Documentation _doc = mapper.readValue(response.getEntity().getContent(), Documentation.class);
            addDoc(_doc);
        }

    }

    public void generateDoc(String templateFile, String outputFile) throws IOException {
        if (outputTemplate.getApiDocuments().isEmpty()) {
            LOG.warn("nothing to write.");
            return;
        }
        LOG.info("Writing doc to " + outputFile + "...");

        Writer writer = new FileWriter(outputFile);
        MustacheFactory mf = new DefaultMustacheFactory();
        Mustache mustache = mf.compile(templateFile);
        mustache.execute(writer, outputTemplate).flush();


        LOG.info("Done!");
    }

    public void addDocs(String basePath, String apiVersion, Collection<Documentation> docs) throws Exception {
        outputTemplate.setBasePath(basePath);
        outputTemplate.setApiVersion(apiVersion);
        for (Documentation doc : docs) {
            addDoc(doc);
        }
    }

    private void addDoc(Documentation doc) throws Exception {
        MformatApiDocument mdoc = new MformatApiDocument(doc);
        mdoc.addTo(outputTemplate);


    }

    class LogAdapter {
        Object logger;

        public LogAdapter(org.slf4j.Logger logger) {
            this.logger = logger;

        }

        public LogAdapter(Log log) {
            this.logger = log;
        }

        private void invoke(String methodName, String s) {
            try {
                Method[] infoMethods = logger.getClass().getDeclaredMethods();
                for (Method m : infoMethods) {
                    if (!m.getName().equals(methodName)) continue;
                    Class<?>[] types = m.getParameterTypes();
                    if (types.length == 1 && CharSequence.class.isAssignableFrom(types[0])) {
                        m.invoke(logger, s);
                        return;
                    }
                }

            } catch (Exception e) {
                System.out.println(s);
            }
        }

        public void info(String s) {
            invoke("info", s);
        }

        public void error(String s) {
            invoke("error", s);
        }

        public void warn(String s) {
            invoke("warn", s);
        }
    }
}

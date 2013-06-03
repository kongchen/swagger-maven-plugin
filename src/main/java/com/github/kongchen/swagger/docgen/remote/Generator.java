package com.github.kongchen.swagger.docgen.remote;


import com.github.kongchen.swagger.docgen.AbstractDocumentSource;
import com.github.kongchen.swagger.docgen.GenerateException;
import com.github.kongchen.swagger.docgen.LogAdapter;
import com.github.kongchen.swagger.docgen.mustache.OutputTemplate;

import java.net.URI;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

/**
 * Created with IntelliJ IDEA.
 *
 * @author: chekong
 */
public class Generator {

    public static void main(String args[]) throws Exception, GenerateException {
        BasicConfigurator.configure();
        Logger logger = Logger.getLogger(Generator.class);

        String outputTpl = "html.mustache";
        String outputPath = "apidoc.html";
        String requestURL = "http://petstore.swagger.wordnik.com/api/api-docs.json?api_key=special-key";
        String swaggerOutput = "api-docs/";

        if (args.length != 4 && args.length !=3) {
            System.out.println("args: swagger-doc-url output-template-path output-file-path [swagger-outputPath]");
            System.out.println("\ttemplate json-schema:\n\t\t" + OutputTemplate.getJsonSchema());
            System.out.println(String.format("Example: %s %s %s %s %s",
                    Generator.class.getName(), requestURL, outputTpl, outputPath, swaggerOutput));
            return;
        }
        requestURL = args[0];
        outputTpl = args[1];
        outputPath = args[2];
        if (args.length == 4) {
            swaggerOutput = args[3];
        }

        URI url = new URI(requestURL);
        AbstractDocumentSource docSource = new RemoteDocumentSource(new LogAdapter(logger), url, outputTpl, outputPath, swaggerOutput);
        docSource.loadDocuments();
        docSource.toDocuments();
        docSource.toSwaggerDocuments();
    }
}

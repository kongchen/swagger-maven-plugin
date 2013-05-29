package com.github.kongchen.swagger.docgen.remote;


import com.github.kongchen.swagger.docgen.AbstractDocumentSource;
import com.github.kongchen.swagger.docgen.GenerateException;
import com.github.kongchen.swagger.docgen.mustache.OutputTemplate;
import com.github.kongchen.swagger.docgen.mustache.TemplateOutputWriter;

import java.net.URI;

/**
 * Created with IntelliJ IDEA.
 *
 * @author: chekong
 */
public class Generator {

    public static void main(String args[]) throws Exception, GenerateException {
        org.apache.log4j.BasicConfigurator.configure();
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
        TemplateOutputWriter templateOutputWriter = new TemplateOutputWriter(outputTpl, outputPath);
        AbstractDocumentSource docSource = new RemoteDocumentSource(url);
        docSource.documentsIn();

        templateOutputWriter.writeBy(new OutputTemplate(docSource));
        if (swaggerOutput != null) {
            docSource.writeSwaggerDocuments(swaggerOutput);
        }

    }
}

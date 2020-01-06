package com.github.kongchen.swagger.docgen.mavenplugin;

import com.github.kongchen.swagger.docgen.AbstractDocumentSource;
import com.github.kongchen.swagger.docgen.reader.SpringMvcApiReader;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

/**
 * @author tedleman
 * 01/21/15
 * @author chekong
 * 05/13/2013
 */
public class SpringMavenDocumentSource extends AbstractDocumentSource<SpringMvcApiReader> {

    public SpringMavenDocumentSource(ApiSource apiSource, Log log, String encoding, String specification, String specificationVersion) throws MojoFailureException {
        super(log, apiSource, encoding, specification, specificationVersion);
    }

    @Override
    protected Set<Class<?>> getValidClasses() {
        Set<Class<?>> result = super.getValidClasses();
        result.addAll(apiSource.getValidClasses(RestController.class));
        result.addAll(apiSource.getValidClasses(ControllerAdvice.class));
        return result;
    }

    @Override
    protected SpringMvcApiReader createReader() {
        return new SpringMvcApiReader(swagger, LOG);
    }
}

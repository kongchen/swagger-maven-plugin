package com.github.kongchen.swagger.docgen.mavenplugin;

import java.util.Set;

import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.RestController;

import com.github.kongchen.swagger.docgen.AbstractDocumentSource;
import com.github.kongchen.swagger.docgen.GenerateException;
import com.github.kongchen.swagger.docgen.reader.ClassSwaggerReader;
import com.github.kongchen.swagger.docgen.reader.SpringMvcApiReader;

/**
 * @author tedleman
 *         01/21/15
 * @author chekong
 * 05/13/2013
 */
public class SpringMavenDocumentSource extends AbstractDocumentSource {

    public SpringMavenDocumentSource(final ApiSource apiSource, final Log log, final String encoding) throws MojoFailureException {
        super(log, apiSource);
        if(encoding !=null) {
            this.encoding = encoding;
        }
    }

    @Override
    protected Set<Class<?>> getValidClasses() {
        final Set result = super.getValidClasses();
        result.addAll(apiSource.getValidClasses(RestController.class));
        result.addAll(apiSource.getValidClasses(ControllerAdvice.class));
        return result;
    }

    @Override
    protected ClassSwaggerReader resolveApiReader() throws GenerateException {
        final String customReaderClassName = apiSource.getSwaggerApiReader();
        if (customReaderClassName == null) {
            final SpringMvcApiReader reader = new SpringMvcApiReader(swagger, LOG);
            reader.setTypesToSkip(this.typesToSkip);
            return reader;
        } else {
            return getCustomApiReader(customReaderClassName);
        }
    }

}

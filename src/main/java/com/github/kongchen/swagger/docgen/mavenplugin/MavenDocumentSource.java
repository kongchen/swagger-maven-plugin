package com.github.kongchen.swagger.docgen.mavenplugin;

import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;

import com.github.kongchen.swagger.docgen.AbstractDocumentSource;
import com.github.kongchen.swagger.docgen.GenerateException;
import com.github.kongchen.swagger.docgen.reader.ClassSwaggerReader;
import com.github.kongchen.swagger.docgen.reader.JaxrsReader;

/**
 * @author chekong
 *         05/13/2013
 */
public class MavenDocumentSource extends AbstractDocumentSource {

    public MavenDocumentSource(ApiSource apiSource, Log log, String encoding) throws MojoFailureException {
        super(log, apiSource);
        if(encoding !=null) {
            this.encoding = encoding;
        }
    }

    protected ClassSwaggerReader resolveApiReader() throws GenerateException {
        String customReaderClassName = apiSource.getSwaggerApiReader();
        if (customReaderClassName == null) {
            JaxrsReader reader = new JaxrsReader(swagger, LOG);
            reader.setTypesToSkip(this.typesToSkip);
            return reader;
        } else {
            return getCustomApiReader(customReaderClassName);
        }
    }
}

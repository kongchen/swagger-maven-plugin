package com.github.kongchen.swagger.docgen.mavenplugin;

import com.google.common.collect.Sets;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;

import com.github.kongchen.swagger.docgen.AbstractDocumentSource;
import com.github.kongchen.swagger.docgen.GenerateException;
import com.github.kongchen.swagger.docgen.reader.AbstractReader;
import com.github.kongchen.swagger.docgen.reader.ClassSwaggerReader;
import com.github.kongchen.swagger.docgen.reader.JaxrsReader;

import javax.ws.rs.Path;
import java.util.Set;

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

    @Override
    protected Set<Class<?>> getValidClasses() {
        return Sets.union(
                super.getValidClasses(),
                apiSource.getValidClasses(Path.class));
    }

    @Override
    protected ClassSwaggerReader resolveApiReader() throws GenerateException {
        String customReaderClassName = apiSource.getSwaggerApiReader();
        if (customReaderClassName == null) {
            JaxrsReader reader = new JaxrsReader(swagger, LOG);
            reader.setTypesToSkip(this.typesToSkip);
            reader.setOperationIdFormat(this.apiSource.getOperationIdFormat());
            return reader;
        } else {
            ClassSwaggerReader customApiReader = getCustomApiReader(customReaderClassName);
            if (customApiReader instanceof AbstractReader) {
                ((AbstractReader)customApiReader).setOperationIdFormat(this.apiSource.getOperationIdFormat());
            }
            return customApiReader;

        }
    }
}

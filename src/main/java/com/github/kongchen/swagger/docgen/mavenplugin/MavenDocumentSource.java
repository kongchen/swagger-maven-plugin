package com.github.kongchen.swagger.docgen.mavenplugin;

import com.google.common.collect.Sets;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;

import com.github.kongchen.swagger.docgen.AbstractDocumentSource;
import com.github.kongchen.swagger.docgen.GenerateException;
import com.github.kongchen.swagger.docgen.reader.ClassSwaggerReader;
import com.github.kongchen.swagger.docgen.reader.JaxrsReader;

import javax.ws.rs.Path;
import java.util.Set;

/**
 * @author chekong
 *         05/13/2013
 */
public class MavenDocumentSource extends AbstractDocumentSource {

    public MavenDocumentSource(final ApiSource apiSource, final Log log, final String encoding) throws MojoFailureException {
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
        final String customReaderClassName = apiSource.getSwaggerApiReader();
        if (customReaderClassName == null) {
            final JaxrsReader reader = new JaxrsReader(swagger, LOG);
            reader.setTypesToSkip(this.typesToSkip);
            return reader;
        } else {
            return getCustomApiReader(customReaderClassName);
        }
    }
}

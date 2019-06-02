package com.github.kongchen.swagger.docgen.mavenplugin;

import com.github.kongchen.swagger.docgen.AbstractDocumentSource;
import com.github.kongchen.swagger.docgen.reader.JaxrsReader;
import com.google.common.collect.Sets;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;

import javax.ws.rs.Path;
import java.util.Set;

/**
 * @author chekong
 * 05/13/2013
 */
public class MavenDocumentSource extends AbstractDocumentSource<JaxrsReader> {

    public MavenDocumentSource(ApiSource apiSource, Log log, String encoding) throws MojoFailureException {
        super(log, apiSource, encoding);
    }

    @Override
    protected Set<Class<?>> getValidClasses() {
        return Sets.union(
                super.getValidClasses(),
                apiSource.getValidClasses(Path.class));
    }

    @Override
    protected JaxrsReader createReader() {
        return new JaxrsReader(swagger, LOG);
    }
}

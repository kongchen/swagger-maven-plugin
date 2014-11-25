package com.github.kongchen.swagger.maven;

import com.github.kongchen.swagger.AbstractDocumentSource;
import com.github.kongchen.swagger.GenerateException;
import com.github.kongchen.swagger.LogAdapter;
import com.wordnik.swagger.config.FilterFactory;
import com.wordnik.swagger.core.filter.SpecFilter;
import com.wordnik.swagger.core.filter.SwaggerSpecFilter;
import com.wordnik.swagger.jaxrs.Reader;
import com.wordnik.swagger.models.Scheme;
import com.wordnik.swagger.models.Swagger;
import org.apache.maven.plugin.logging.Log;

import java.util.HashMap;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 *
 * @author: chekong
 * 05/13/2013
 */
public class MavenDocumentSource extends AbstractDocumentSource {
    private final ApiSource apiSource;

    public MavenDocumentSource(ApiSource apiSource, Log log) {
        super(new LogAdapter(log),
                apiSource.getOutputPath(), apiSource.getTemplatePath(),
                apiSource.getSwaggerDirectory(), apiSource.getOverridingModels());
        swagger = new Swagger();
        if (apiSource.getSchemes() != null) {
            if (apiSource.getSchemes().contains(",")) {
                for(String scheme: apiSource.getSchemes().split(",")) {
                    swagger.scheme(Scheme.forValue(scheme));
                }
            } else {
                swagger.scheme(Scheme.forValue(apiSource.getSchemes()));
            }
        }

        swagger.setHost(apiSource.getHost());
        swagger.setInfo(apiSource.getInfo());
        swagger.setBasePath(apiSource.getBasePath());

        this.apiSource = apiSource;
    }

    @Override
    public void loadDocuments() throws GenerateException {
        if (apiSource.getSwaggerInternalFilter() != null) {
            try {
                LOG.info("Setting filter configuration: " + apiSource.getSwaggerInternalFilter());
                FilterFactory.setFilter((SwaggerSpecFilter) Class.forName(apiSource.getSwaggerInternalFilter()).newInstance());
            } catch (Exception e) {
                throw new GenerateException("Cannot load: " + apiSource.getSwaggerInternalFilter(), e);
            }
        }
        swagger = new Reader(swagger).read(apiSource.getValidClasses());


        if (FilterFactory.getFilter() != null) {
            swagger = new SpecFilter().filter(swagger, FilterFactory.getFilter(),
                new HashMap<String, List<String>>(), new HashMap<String, String>(),
                new HashMap<String, List<String>>());
        }

    }
}

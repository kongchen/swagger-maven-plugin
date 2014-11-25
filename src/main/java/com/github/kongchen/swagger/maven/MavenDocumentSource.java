package com.github.kongchen.swagger.maven;

import com.github.kongchen.swagger.AbstractDocumentSource;
import com.github.kongchen.swagger.ApiReader;
import com.github.kongchen.swagger.GenerateException;
import com.github.kongchen.swagger.LogAdapter;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.config.FilterFactory;
import com.wordnik.swagger.core.filter.SpecFilter;
import com.wordnik.swagger.core.filter.SwaggerSpecFilter;
import com.wordnik.swagger.models.Model;
import com.wordnik.swagger.models.Path;
import com.wordnik.swagger.models.Scheme;
import com.wordnik.swagger.models.Swagger;
import com.wordnik.swagger.models.auth.SecurityScheme;
import org.apache.maven.plugin.logging.Log;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        for (Class c : apiSource.getValidClasses()) {
            Swagger sw;
            try {
                sw = getDocFromClass(c);
            } catch (Exception e) {
                throw new GenerateException(e);
            }

            if (sw == null) continue;

            LOG.info("Detect Resource:" + c.getName());
            if (sw.getSecurityDefinitions() != null) {
                for (Map.Entry<String, SecurityScheme> entry : sw.getSecurityDefinitions().entrySet()) {
                    this.swagger.securityDefinition(entry.getKey(), entry.getValue());
                }
            }
            if (sw.getPaths() != null) {
                if (this.swagger.getPaths() == null) {
                    this.swagger.paths(sw.getPaths());
                } else {
                    for (Map.Entry<String, Path> entry : sw.getPaths().entrySet()) {
                        this.swagger.getPaths().put(entry.getKey(), entry.getValue());
                    }
                }
            }

            if (sw.getDefinitions() != null) {
                for (Map.Entry<String, Model> entry : sw.getDefinitions().entrySet()) {

                    swagger.addDefinition(entry.getKey(), entry.getValue());
                }
            }
        }

        swagger.setBasePath(getBasePath());
        swagger.setInfo(apiSource.getInfo());

        if (FilterFactory.getFilter() != null) {
            swagger = new SpecFilter().filter(swagger, FilterFactory.getFilter(),
                new HashMap<String, List<String>>(), new HashMap<String, String>(),
                new HashMap<String, List<String>>());
        }

    }

    private Swagger getDocFromClass(Class c) throws Exception {
        Api resource = (Api) c.getAnnotation(Api.class);

        if (resource == null) return null;

        Swagger swagger = getApiReader().read(c);

        return swagger;
    }

    private ApiReader getApiReader() throws Exception {
        if (apiSource.getSwaggerApiReader() == null) return new DefaultJaxrsReader();
        try {
            LOG.info("Reading api reader configuration: " + apiSource.getSwaggerApiReader());
            return (ApiReader) Class.forName(apiSource.getSwaggerApiReader()).newInstance();
        } catch (Exception e) {
            throw new GenerateException("Cannot load swagger api reader: "
                    + apiSource.getSwaggerApiReader(), e);
        }
    }
}

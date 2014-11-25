package com.github.kongchen.swagger.maven;

import com.github.kongchen.swagger.docgen.AbstractDocumentSource;
import com.github.kongchen.smp.ApiReader;
import com.github.kongchen.swagger.docgen.GenerateException;
import com.github.kongchen.swagger.docgen.LogAdapter;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.config.FilterFactory;
import com.wordnik.swagger.core.filter.SpecFilter;
import com.wordnik.swagger.core.filter.SwaggerSpecFilter;
import com.wordnik.swagger.models.Swagger;
import org.apache.maven.plugin.logging.Log;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 *
 * @author: chekong
 * 05/13/2013
 */
public class MavenDocumentSource extends AbstractDocumentSource {
    private final ApiSource apiSource;

    private final SpecFilter specFilter = new SpecFilter();

    public MavenDocumentSource(ApiSource apiSource, Log log) {
        super(new LogAdapter(log),
                apiSource.getOutputPath(), apiSource.getOutputTemplate(), apiSource.getSwaggerDirectory(), apiSource.mustacheFileRoot, apiSource.isUseOutputFlatStructure(), apiSource.getOverridingModels());

        setApiVersion(apiSource.getApiVersion());
        setBasePath(apiSource.getBasePath());
        setApiInfo(apiSource.getInfo());
        this.apiSource = apiSource;
    }

    @Override
    public void loadDocuments() throws GenerateException {
        swagger = new Swagger();

        if (apiSource.getSwaggerInternalFilter() != null) {
            try {
                LOG.info("Setting filter configuration: " + apiSource.getSwaggerInternalFilter());
                FilterFactory.setFilter((SwaggerSpecFilter) Class.forName(apiSource.getSwaggerInternalFilter()).newInstance());
            } catch (Exception e) {
                throw new GenerateException("Cannot load: " + apiSource.getSwaggerInternalFilter(), e);
            }
        }

        for (Class c : apiSource.getValidClasses()) {
            Swagger swagger;
            try {
                 swagger = getDocFromClass(c);
            } catch (Exception e) {
                throw new GenerateException(e);
            }

            if (swagger == null) continue;

            LOG.info("Detect Resource:" + c.getName());
            swagger.getSecurityDefinitions().forEach(this.swagger::securityDefinition);
            swagger.getPaths().forEach(this.swagger.getPaths()::put);
            swagger.getDefinitions().forEach(this.swagger::addDefinition);
        }

        swagger.setBasePath(getBasePath());
        swagger.setInfo(apiSource.getInfo());

    }

//    private Option<ApiInfo> toSwaggerApiInfo(ApiSourceInfo info) {
//        if (info == null) return Option.empty();
//        return Option.apply(new ApiInfo(info.getTitle(), info.getDescription(),
//            info.getTermsOfServiceUrl(), info.getContact(),
//            info.getLicense(), info.getLicenseUrl()));
//    }

    private Swagger getDocFromClass(Class c) throws Exception {
        Api resource = (Api) c.getAnnotation(Api.class);

        if (resource == null) return null;


        ApiReader reader = getApiReader();
        Swagger swagger = reader.read(c);


        return specFilter.filter(swagger, FilterFactory.getFilter(), new HashMap<String, List<String>>(),
                new HashMap<String, String>(), new HashMap<String, List<String>>());

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

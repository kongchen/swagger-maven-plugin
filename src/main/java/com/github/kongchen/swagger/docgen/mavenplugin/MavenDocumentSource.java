package com.github.kongchen.swagger.docgen.mavenplugin;

import com.github.kongchen.swagger.docgen.AbstractDocumentSource;
import com.github.kongchen.swagger.docgen.GenerateException;
import com.github.kongchen.swagger.docgen.LogAdapter;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.config.FilterFactory;
import com.wordnik.swagger.config.FilterFactory$;
import com.wordnik.swagger.config.SwaggerConfig;
import com.wordnik.swagger.core.SwaggerSpec;
import com.wordnik.swagger.core.filter.SpecFilter;
import com.wordnik.swagger.core.filter.SwaggerSpecFilter;
import com.wordnik.swagger.jaxrs.reader.DefaultJaxrsApiReader;
import com.wordnik.swagger.model.*;
import com.wordnik.swagger.reader.ClassReader;

import org.apache.maven.plugin.logging.Log;

import scala.None;
import scala.Option;
import scala.collection.JavaConversions;
import scala.collection.immutable.Map$;
import scala.collection.mutable.Buffer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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
        super(new LogAdapter(log), apiSource.getOutputPath(), apiSource.getOutputTemplate(),
                apiSource.getSwaggerDirectory(), apiSource.mustacheFileRoot, apiSource.isUseOutputFlatStructure(),
                apiSource.getOverridingModels(), apiSource.getApiSortComparator());

        setApiVersion(apiSource.getApiVersion());
        setBasePath(apiSource.getBasePath());
        setApiInfo(apiSource.getApiInfo());
        this.apiSource = apiSource;
    }

    @Override
    public void loadDocuments() throws GenerateException {
        SwaggerConfig swaggerConfig = new SwaggerConfig();
        swaggerConfig.setApiVersion(apiSource.getApiVersion());
        swaggerConfig.setSwaggerVersion(SwaggerSpec.version());
        List<ApiListingReference> apiListingReferences = new ArrayList<ApiListingReference>();

        if (apiSource.getSwaggerInternalFilter() != null) {
            FilterFactory$ filterFactory = FilterFactory$.MODULE$;
            try {
                LOG.info("Setting filter configuration: " + apiSource.getSwaggerInternalFilter());
                filterFactory.filter_$eq((SwaggerSpecFilter) Class.forName(apiSource.getSwaggerInternalFilter()).newInstance());
            } catch (Exception e) {
                throw new GenerateException("Cannot load: " + apiSource.getSwaggerInternalFilter(), e);
            }
        }

        List<AuthorizationType> authorizationTypes = new ArrayList<AuthorizationType>();
        for (Class c : apiSource.getValidClasses()) {
            ApiListing doc;
            try {
                doc = getDocFromClass(c, swaggerConfig, getBasePath());
            } catch (Exception e) {
                throw new GenerateException(e);
            }
            if (doc == null) continue;
            LOG.info("Detect Resource:" + c.getName());

            Buffer<AuthorizationType> buffer = doc.authorizations().toBuffer();
            authorizationTypes.addAll(JavaConversions.asJavaList(buffer));
            ApiListingReference apiListingReference = new ApiListingReference(doc.resourcePath(), doc.description(), doc.position());
            apiListingReferences.add(apiListingReference);
            acceptDocument(doc);
        }
        // sort apiListingRefernce by position
        Collections.sort(apiListingReferences, new Comparator<ApiListingReference>() {
            @Override
            public int compare(ApiListingReference o1, ApiListingReference o2) {
                if (o1 == null && o2 == null) return 0;
                if (o1 == null && o2 != null) return -1;
                if (o1 != null && o2 == null) return 1;
                return o1.position() - o2.position();
            }
        });
        serviceDocument = new ResourceListing(swaggerConfig.apiVersion(), swaggerConfig.swaggerVersion(),
                                              scala.collection.immutable.List.fromIterator(JavaConversions.asScalaIterator(apiListingReferences.iterator())),
                                              scala.collection.immutable.List.fromIterator(JavaConversions.asScalaIterator(authorizationTypes.iterator())),
                                              toSwaggerApiInfo(apiSource.getApiInfo()));
    }

    private Option<ApiInfo> toSwaggerApiInfo(ApiSourceInfo info) {
        if (info == null) return Option.empty();
        return Option.apply(new ApiInfo(info.getTitle(), info.getDescription(),
            info.getTermsOfServiceUrl(), info.getContact(),
            info.getLicense(), info.getLicenseUrl()));
    }

    private ApiListing getDocFromClass(Class c, SwaggerConfig swaggerConfig, String basePath) throws Exception {
        Api resource = (Api) c.getAnnotation(Api.class);

        if (resource == null) return null;
        ClassReader reader = getApiReader();
        Option<ApiListing> apiListing = reader.read(basePath, c, swaggerConfig);

        if (None.canEqual(apiListing)) return null;

        return specFilter.filter(apiListing.get(), FilterFactory.filter(),
                                 Map$.MODULE$.<String, scala.collection.immutable.List<String>>empty(),
                                 Map$.MODULE$.<String, String>empty(),
                                 Map$.MODULE$.<String, scala.collection.immutable.List<String>>empty());
    }

	private ClassReader getApiReader() throws Exception {
		if (apiSource.getSwaggerApiReader() == null) return new DefaultJaxrsApiReader();
		try {
			LOG.info("Reading api reader configuration: " + apiSource.getSwaggerApiReader());
			return (ClassReader) Class.forName(apiSource.getSwaggerApiReader()).newInstance();
		} catch (Exception e) {
			throw new GenerateException("Cannot load swagger api reader: "
					+ apiSource.getSwaggerApiReader(), e);
		}
	}
}

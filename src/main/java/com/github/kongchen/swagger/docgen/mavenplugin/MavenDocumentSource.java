package com.github.kongchen.swagger.docgen.mavenplugin;

import com.github.kongchen.swagger.docgen.AbstractDocumentSource;
import com.github.kongchen.swagger.docgen.GenerateException;
import com.github.kongchen.swagger.docgen.LogAdapter;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.config.SwaggerConfig;
import com.wordnik.swagger.core.SwaggerSpec;
import com.wordnik.swagger.jaxrs.JaxrsApiReader;
import com.wordnik.swagger.jaxrs.reader.DefaultJaxrsApiReader;
import com.wordnik.swagger.model.ApiListing;
import com.wordnik.swagger.model.ApiListingReference;
import com.wordnik.swagger.model.AuthorizationType;
import com.wordnik.swagger.model.ResourceListing;
import org.apache.maven.plugin.logging.Log;
import scala.None;
import scala.Option;
import scala.collection.JavaConversions;
import scala.collection.mutable.Buffer;

import java.util.ArrayList;
import java.util.List;

import static java.util.AbstractMap.SimpleEntry;

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
                apiSource.getOutputPath(), apiSource.getOutputTemplate(), apiSource.getSwaggerDirectory(), apiSource.mustacheFileRoot, apiSource.isUseOutputFlatStructure());

        setApiVersion(apiSource.getApiVersion());
        setBasePath(apiSource.getBasePath());
        this.apiSource = apiSource;
    }

    @Override
    public void loadDocuments() throws GenerateException {
        SwaggerConfig swaggerConfig =  new SwaggerConfig();
        swaggerConfig.setApiVersion(apiSource.getApiVersion());
        swaggerConfig.setSwaggerVersion(SwaggerSpec.version());
        List<ApiListingReference> apiListingReferences = new ArrayList<ApiListingReference>();
        List<AuthorizationType> authorizationTypes = new ArrayList<AuthorizationType>();
        for (Class c : apiSource.getValidClasses()) {
            ApiListing doc;
            try {
                doc  = getDocFromClass(c, swaggerConfig, getBasePath());
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
        serviceDocument = new ResourceListing(swaggerConfig.apiVersion(), swaggerConfig.swaggerVersion(),
                scala.collection.immutable.List.fromIterator(JavaConversions.asScalaIterator(apiListingReferences.iterator())),
                scala.collection.immutable.List.fromIterator(JavaConversions.asScalaIterator(authorizationTypes.iterator())),
                swaggerConfig.info());
    }

    private ApiListing getDocFromClass(Class c, SwaggerConfig swaggerConfig, String basePath) throws Exception {
        Api resource = (Api) c.getAnnotation(Api.class);

        if (resource == null) return null;
        JaxrsApiReader reader = new DefaultJaxrsApiReader();
        Option<ApiListing> apiListing = reader.read(basePath, c, swaggerConfig);

        if (None.canEqual(apiListing)) return null;

        return apiListing.get();
    }
}

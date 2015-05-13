package com.github.kongchen.swagger.docgen.mavenplugin;

import com.github.kongchen.swagger.docgen.AbstractDocumentSource;
import com.github.kongchen.swagger.docgen.GenerateException;
import com.github.kongchen.swagger.docgen.LogAdapter;
import com.github.kongchen.swagger.docgen.reader.SpringMvcApiReader;
import com.wordnik.swagger.config.FilterFactory;
import com.wordnik.swagger.core.filter.SpecFilter;
import com.wordnik.swagger.core.filter.SwaggerSpecFilter;
import com.wordnik.swagger.models.auth.SecuritySchemeDefinition;
import org.apache.maven.plugin.logging.Log;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author tedleman
 *         01/21/15
 * @author: chekong
 * 05/13/2013
 */
public class SpringMavenDocumentSource extends AbstractDocumentSource {

    private final SpecFilter specFilter = new SpecFilter();

    public SpringMavenDocumentSource(ApiSource apiSource, Log log) {
        super(new LogAdapter(log), apiSource);

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
        swagger = new SpringMvcApiReader(swagger, LOG).read(apiSource.getValidClasses());

        if(apiSource.getSecurityDefinitions() != null) {
            for (SecurityDefinition sd : apiSource.getSecurityDefinitions()) {
                if(sd.getDefinitions().isEmpty()) {
                    continue;
                }
                Iterator<Map.Entry<String, SecuritySchemeDefinition>> it = sd.getDefinitions().entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<String, SecuritySchemeDefinition> entry = it.next();
                    swagger.addSecurityDefinition(entry.getKey(), entry.getValue());
                }
            }
            // sort security defs to make output consistent
            Map<String, SecuritySchemeDefinition> defs = swagger.getSecurityDefinitions();
            Map<String, SecuritySchemeDefinition> sortedDefs = new TreeMap<String, SecuritySchemeDefinition>();
            sortedDefs.putAll(defs);
            swagger.setSecurityDefinitions(sortedDefs);

        }

        if (FilterFactory.getFilter() != null) {
            swagger = new SpecFilter().filter(swagger, FilterFactory.getFilter(),
                    new HashMap<String, List<String>>(), new HashMap<String, String>(),
                    new HashMap<String, List<String>>());
        }


    }
}

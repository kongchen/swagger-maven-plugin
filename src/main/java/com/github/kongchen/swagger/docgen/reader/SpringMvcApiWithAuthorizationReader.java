package com.github.kongchen.swagger.docgen.reader;

import com.github.kongchen.swagger.docgen.spring.SpringResource;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.Swagger;
import org.apache.maven.plugin.logging.Log;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.lang.reflect.Method;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.springframework.core.annotation.AnnotatedElementUtils.findMergedAnnotation;

/**
 * Extends rest api endpoint description with the permissions details that given endpoint requires.
 *
 * This swagger reader is used as {@code swaggerApiReader} property of the {@code swagger-maven-plugin}.
 */
public class SpringMvcApiWithAuthorizationReader extends SpringMvcApiReader {

    private static final String PERMISSIONS_LABEL = "\n\n**Required Permissions**:\n\n";

    public SpringMvcApiWithAuthorizationReader(Swagger swagger, Log log) {
        super(swagger, log);
    }

    @Override
    public Swagger read(SpringResource resource) {
        Swagger extSwagger = super.read(resource);

        List<Method> methods = resource.getMethods();
        for (Method method : methods) {
            PreAuthorize preAuthorize = findMergedAnnotation(method, PreAuthorize.class);
            RequestMapping requestMapping = findMergedAnnotation(method, RequestMapping.class);
            if (preAuthorize == null) continue; // nothing to update
            if (requestMapping == null) continue; // nothing to update

            String resourcePathKey = resource.getControllerMapping() + resource.getResourceName();
            Path path = extSwagger.getPath(resourcePathKey);
            if (path == null) continue; // nothing to update

            String permissions = preAuthorize.value();
            if (isBlank(permissions)) continue; // nothing to update

            for (RequestMethod reqMethod : requestMapping.method()) {
                Operation operation = operation(path, reqMethod);
                updateOperation(operation, permissions);
            }
        }

        return extSwagger;
    }

    private static Operation operation(Path path, RequestMethod method) {
        switch (method) {
            case POST:
                return path.getPost();
            case GET:
                return path.getGet();
            case PUT:
                return path.getPut();
            case DELETE:
                return path.getDelete();
            case HEAD:
                return path.getHead();
            case OPTIONS:
                return path.getOptions();
            case PATCH:
                return path.getPatch();
            default:
                throw new IllegalArgumentException("could not find operation for method " + method);
        }
    }

    private static void updateOperation(Operation operation, String permissions) {
        String updatedDescription = operation.getDescription() + PERMISSIONS_LABEL + permissions;
        operation.description(updatedDescription);
    }
}

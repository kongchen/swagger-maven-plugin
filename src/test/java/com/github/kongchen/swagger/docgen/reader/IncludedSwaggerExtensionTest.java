package com.github.kongchen.swagger.docgen.reader;

import com.github.kongchen.swagger.docgen.jaxrs.BeanParamInjectParamExtention;
import com.github.kongchen.swagger.docgen.jaxrs.JaxrsParameterExtension;
import com.github.kongchen.swagger.docgen.spring.SpringSwaggerExtension;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.reflect.ClassPath;
import com.wordnik.sample.TestVendorExtension;
import io.swagger.jaxrs.ext.AbstractSwaggerExtension;
import io.swagger.jaxrs.ext.SwaggerExtension;
import io.swagger.models.parameters.Parameter;
import org.mockito.ArgumentMatcher;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import javax.swing.text.html.HTMLDocument;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.*;

import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

/**
 * Test class which ensures common functionality across all of the currently included Swagger Extensions, namely </p>
 * <ul>
 *     <li>@{@link com.github.kongchen.swagger.docgen.spring.SpringSwaggerExtension}</li>
 *     <li>{@link com.github.kongchen.swagger.docgen.jaxrs.JaxrsParameterExtension}</li>
 *     <li>{@link com.github.kongchen.swagger.docgen.jaxrs.BeanParamInjectParamExtention}</li>
 * </ul>
 *
 */
public class IncludedSwaggerExtensionTest {
    private static final List<AbstractSwaggerExtension> SWAGGER_EXTENSIONS = Lists.newArrayList();

    static {
        //TODO: Maybe use a Classpath Scanner to automatically figure out the included extensions?
        SWAGGER_EXTENSIONS.add(new JaxrsParameterExtension());
        SWAGGER_EXTENSIONS.add(new SpringSwaggerExtension());
        SWAGGER_EXTENSIONS.add(new BeanParamInjectParamExtention(mock(JaxrsReader.class)));
    }

    @Test
    /**
     * This tests acts more like an integration test than a real unit test. It tests whether the extensions return
     * generally correct values.
     */
    public void testExtractParametersReturnsEmptyList() {
        for (AbstractSwaggerExtension swaggerExtension : SWAGGER_EXTENSIONS) {
            Set<Type> typesToSkip = Collections.emptySet();
            List<Annotation> annotations = Collections.emptyList();
            AbstractSwaggerExtension extension = mock(AbstractSwaggerExtension.class, CALLS_REAL_METHODS);
            Iterator<SwaggerExtension> iterator = Lists.<SwaggerExtension>newArrayList(extension).iterator();

            // By default, AbstractSwaggerExtension.extractParameter returns an immutable collection
            // This is not desirable for this test, since in the real world every extension should return a modifiable one.
            // To ensure compatibly with other, third party extensions
            when(extension.extractParameters(
                    annotations,
                    Void.TYPE,
                    typesToSkip,
                    iterator)
            ).thenReturn(Lists.<Parameter>newArrayList());

            // Not possible to add any parameters for the extensions, since no annotation / field is given to the extensions
            // only the previously created mock AbstractSwaggerExtension is in the chain
            // This allows to test if first the chain is called, and only then empty, modifiable lists are returned as last resort
            List<Parameter> parameters = swaggerExtension.extractParameters(
                    annotations,
                    Void.TYPE,
                    typesToSkip,
                    iterator);

            // returned parameters have to be empty since we gave the extension no real type to work with
            assertTrue(parameters.isEmpty(), "Extension " + swaggerExtension.getClass().getName() + " did not return a modifiable list.");

            try {
                verify(extension).extractParameters(
                        anyListOf(Annotation.class),
                        any(Type.class),
                        anySetOf(Type.class),
                        eq(iterator)
                );
                // This will throw an exception if the parameters list is not modifiable, thus failing this test
                parameters.add(null);
            } catch (Throwable t) {
                // Catch everything here.
                // We need to output the currently tested extension here
                // so that the reason why the test failed can be easier recognized later on
                // Still need to rethrow the exception though, to make the test fail

                // TODO: Is there any better wrapper exception type?
                throw new IllegalStateException("Extension "+ swaggerExtension.getClass().getName() + " failed this Test.", t);
            }
        }
    }
}

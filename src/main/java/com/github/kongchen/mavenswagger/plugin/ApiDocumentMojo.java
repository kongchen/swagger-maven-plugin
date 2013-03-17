package com.github.kongchen.mavenswagger.plugin;

import com.github.kongchen.mavenswagger.apigen.ApiDocumentGenerator;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.core.Documentation;
import com.wordnik.swagger.core.SwaggerSpec;
import com.wordnik.swagger.jaxrs.HelpApi;
import com.wordnik.swagger.jaxrs.JaxrsApiSpecParser;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;


/**
 * Created with IntelliJ IDEA.
 *
 * @author: chekong
 */

/**
 * @goal generate
 * @phase package
 * @configurator include-project-dependencies
 * @requiresDependencyResolution runtime
 */
public class ApiDocumentMojo extends AbstractMojo {
    /**
     * @parameter
     */
    private List<ApiSource> apiSources;

    private Set<String> outputSet = new HashSet<String>();

    /**
     * Recursive method used to find all classes in a given directory and subdirs.
     *
     * @param directory   The base directory
     * @param packageName The package name for classes found inside the base directory
     * @return The classes
     * @throws ClassNotFoundException
     */
    private static List<Class> findClasses(File directory, String packageName) throws ClassNotFoundException {
        List<Class> classes = new ArrayList<Class>();
        if (!directory.exists()) {
            return classes;
        }
        File[] files = directory.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                assert !file.getName().contains(".");
                classes.addAll(findClasses(file, packageName + "." + file.getName()));
            } else if (file.getName().endsWith(".class")) {
                classes.add(Class.forName(packageName + '.' + file.getName().substring(0,
                        file.getName().length() - 6)));
            }
        }
        return classes;
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        try {
            for (ApiSource apiSource : apiSources) {
                ApiDocumentGenerator apiGenerator = new ApiDocumentGenerator(getLog());
                apiGenerator.addDocs(apiSource.getBasePath(), apiSource.getApiVersion(),
                        lookupDocumentsInSource(apiSource));
                if (!outputSet.add(new File(apiSource.getOutputPath()).getAbsolutePath())) {
                    throw new MojoExecutionException("output path " + apiSource.getOutputPath() + " is specified to " +
                            "more than one api");
                }
                apiGenerator.generateDoc(apiSource.getOutputTemplate(), apiSource.getOutputPath());
            }
        } catch (Exception e) {
            throw new MojoFailureException(e.getMessage());
        }
    }

    private Documentation getDocFromClass(Class c, String apiVersion, String basePath) throws Exception {
        Api resource = (Api) c.getAnnotation(Api.class);

        if (resource == null) return null;
        getLog().info("Generate from " + c.getName());

        JaxrsApiSpecParser parser = new JaxrsApiSpecParser(c, apiVersion,
                SwaggerSpec.version(), basePath, resource.value());

        return new HelpApi().filterDocs(parser.parse(), null, null, null, null);
    }

    private List<Documentation> lookupDocumentsInSource(ApiSource apiSource) throws Exception {
        List<Documentation> docs = new LinkedList<Documentation>();
        Collection<Class> classes = new HashSet<Class>();
        if (apiSource.getApiClasses() != null) {
            for (String cname : apiSource.getApiClasses()) {
                Class c = Class.forName(cname);
                classes.add(c);
            }
        }

        if (apiSource.getApiPackage() != null) {
            classes.addAll(getClasses(apiSource.getApiPackage()));
        }

        for (Class c : classes) {
            Documentation doc = getDocFromClass(c, apiSource.getApiVersion(), apiSource.getBasePath());
            if (doc == null) continue;
            getLog().info("add " + c.getName());
            docs.add(doc);
        }

        return docs;
    }

    /**
     * Scans all classes accessible from the context class loader which belong to the given package and subpackages.
     *
     * @param packageName The base package
     * @return The classes
     * @throws ClassNotFoundException
     * @throws IOException
     */
    private Collection<Class> getClasses(String packageName)
            throws ClassNotFoundException, IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        assert classLoader != null;
        String path = packageName.replace('.', '/');
        Enumeration<URL> resources = classLoader.getResources(path);
        List<File> dirs = new ArrayList<File>();
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            dirs.add(new File(resource.getFile()));
        }
        ArrayList<Class> classes = new ArrayList<Class>();
        for (File directory : dirs) {
            classes.addAll(findClasses(directory, packageName));
        }
        return Collections.unmodifiableCollection(classes);
    }
}

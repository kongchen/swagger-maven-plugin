package com.github.kongchen.swagger.docgen;

import com.github.jknack.handlebars.io.ClassPathTemplateLoader;
import com.github.jknack.handlebars.io.FileTemplateLoader;
import io.swagger.models.Model;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.Response;
import io.swagger.models.Swagger;
import io.swagger.models.Tag;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author chekong on 14-11-25.
 */
public class Utils {

    private static final java.lang.String CLASSPATH = "classpath:";

    public static TemplatePath parseTemplateUrl(final String templatePath) throws GenerateException {
        if (templatePath == null) {
            return null;
        }
        final TemplatePath tp;
        if (templatePath.startsWith(CLASSPATH)) {
            final String resPath = templatePath.substring(CLASSPATH.length());
            tp = extractTemplateObject(resPath);
            tp.loader = new ClassPathTemplateLoader(tp.prefix, tp.suffix);
        } else {
            tp = extractTemplateObject(templatePath);
            tp.loader = new FileTemplateLoader(tp.prefix, tp.suffix);
        }

        return tp;
    }

    private static TemplatePath extractTemplateObject(final String resPath) throws GenerateException {
        final TemplatePath tp = new TemplatePath();
        String prefix = "";
        String suffix = "";
        String name = "";

        final int prefixidx = resPath.lastIndexOf("/");
        if (prefixidx != -1) {
            prefix = resPath.substring(0, prefixidx + 1);
        }

        final int extidx = resPath.lastIndexOf(".");
        if (extidx != -1) {
            suffix = resPath.substring(extidx);
            if (extidx < prefix.length()) {
                throw new GenerateException("You have an interesting template path:" + resPath);
            }
            name = resPath.substring(prefix.length(), extidx);
        }
        tp.name = name;
        tp.prefix = prefix;
        tp.suffix = suffix;

        return tp;
    }

    public static void sortSwagger(final Swagger swagger) throws GenerateException {
        if (swagger == null || swagger.getPaths() == null) {
            return;
        }

        final TreeMap<String, Path> sortedMap = new TreeMap<String, Path>();
        if (swagger.getPaths() == null) {
            return;
        }
        sortedMap.putAll(swagger.getPaths());
        swagger.paths(sortedMap);

        for (final Path path : swagger.getPaths().values()) {
            final String[] methods = {"Get", "Delete", "Post", "Put", "Options", "Patch"};
            for (final String m : methods) {
                sortResponses(path, m);
            }
        }

        //reorder definitions
        if (swagger.getDefinitions() != null) {
            final TreeMap<String, Model> defs = new TreeMap<String, Model>();
            defs.putAll(swagger.getDefinitions());
            swagger.setDefinitions(defs);
        }

        // order the tags
        if (swagger.getTags() != null) {
            Collections.sort(swagger.getTags(), new Comparator<Tag>() {
                public int compare(final Tag a, final Tag b) {
                    return a.toString().compareTo(b.toString());
                }
            });
        }

    }

    private static void sortResponses(final Path path, final String method) throws GenerateException {
        try {
            final Method m = Path.class.getDeclaredMethod("get" + method);
            final Operation op = (Operation) m.invoke(path);
            if (op == null) {
                return;
            }
            final Map<String, Response> responses = op.getResponses();
            final TreeMap<String, Response> res = new TreeMap<String, Response>();
            res.putAll(responses);
            op.setResponses(res);
        } catch (final NoSuchMethodException e) {
            throw new GenerateException(e);
        } catch (final InvocationTargetException e) {
            throw new GenerateException(e);
        } catch (final IllegalAccessException e) {
            throw new GenerateException(e);
        }
    }
}

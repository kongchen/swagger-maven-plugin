package com.github.kongchen.swagger;

import com.github.jknack.handlebars.io.ClassPathTemplateLoader;
import com.github.jknack.handlebars.io.FileTemplateLoader;
import com.wordnik.swagger.models.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by chekong on 14-11-25.
 */
public class Utils {

    private static final java.lang.String CLASSPATH = "classpath:";

    public static TemplatePath parseTemplateUrl(String templatePath) throws GenerateException {
        if (templatePath == null) return null;
        TemplatePath tp;
        if (templatePath.startsWith(CLASSPATH)) {
            String resPath = templatePath.substring(CLASSPATH.length());
            tp = extractTemplateObject(resPath);
            tp.loader = new ClassPathTemplateLoader(tp.prefix, tp.suffix);
        } else {
            tp = extractTemplateObject(templatePath);
            tp.loader = new FileTemplateLoader(tp.prefix, tp.suffix);
        }

        return tp;
    }

    private static TemplatePath extractTemplateObject(String resPath) throws GenerateException {
        TemplatePath tp = new TemplatePath();
        String prefix = "";
        String suffix = "";
        String name = "";

        int prefixidx = resPath.lastIndexOf("/");
        if (prefixidx != -1) {
            prefix = resPath.substring(0, prefixidx + 1);
        }

        int extidx = resPath.lastIndexOf(".");
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

    public static void sortSwagger(Swagger swagger) throws GenerateException {
        if (swagger == null) return;
        Comparator<String> strcomp = new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o1.compareTo(o2);
            }
        };
        TreeMap<String, Path> sortedMap = new TreeMap<String, Path>(strcomp);
        sortedMap.putAll(swagger.getPaths());
        swagger.paths(sortedMap);

        for(Path path : swagger.getPaths().values()) {
            String methods[] = {"Get", "Delete", "Post", "Put", "Options", "Patch"};
            for (String m : methods) {
                sortResponses(path, m);
            }
        }

        //reorder definitions
        if (swagger.getDefinitions() != null) {
            TreeMap<String, Model> defs = new TreeMap<String, Model>(strcomp);
            defs.putAll(swagger.getDefinitions());
            swagger.setDefinitions(defs);
        }

    }

    private static void sortResponses(Path path, String method) throws GenerateException {
        try {
            Method m = Path.class.getDeclaredMethod("get" + method);
            Operation op = (Operation) m.invoke(path);
            if (op == null) return;
            Map<String, Response> responses = op.getResponses();
            TreeMap<String, Response> res = new TreeMap<String, Response>();
            res.putAll(responses);
            op.setResponses(res);
        } catch (NoSuchMethodException e) {
            throw new GenerateException(e);
        } catch (InvocationTargetException e) {
            throw new GenerateException(e);
        } catch (IllegalAccessException e) {
            throw new GenerateException(e);
        }
    }
}

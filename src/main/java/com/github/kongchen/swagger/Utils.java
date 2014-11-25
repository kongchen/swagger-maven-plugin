package com.github.kongchen.swagger;

import com.github.jknack.handlebars.io.ClassPathTemplateLoader;
import com.github.jknack.handlebars.io.FileTemplateLoader;

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
}

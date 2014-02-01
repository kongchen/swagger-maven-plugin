package com.github.kongchen.swagger.docgen.mustache;

import com.github.kongchen.swagger.docgen.TypeUtils;
import com.wordnik.swagger.core.util.ModelUtil;
import com.wordnik.swagger.model.Model;
import scala.Option;
import scala.Tuple2;

import java.util.LinkedList;

public class MustacheResponseClass {
    private String className;
    private String classLinkName;
    private LinkedList<MustacheResponseClass> genericClasses;

    public MustacheResponseClass(String responseClass) {
        if ((TypeUtils.genericPattern.matcher(responseClass).matches())) {
            String trueType = TypeUtils.getTrueType(responseClass);
            this.classLinkName = trueType;
            this.className = trueType;
            genericClasses = new LinkedList<MustacheResponseClass>();
            while (true) {
                int idx1 = responseClass.indexOf('<');
                int idx2 = responseClass.lastIndexOf('>');
                if (idx1 == -1 || idx2 == -1) {
                    break;
                }
                responseClass = responseClass.substring(idx1 + 1, idx2);
                if (responseClass.contains(",")) {
                    for (String c : responseClass.split(",")){
                        genericClasses.add(new MustacheResponseClass(c));
                    }
                } else {
                    genericClasses.add(new MustacheResponseClass(responseClass));
                }
            }
        } else {
            String trueType = TypeUtils.getTrueType(responseClass);
            Option<Tuple2<String, Model>> m = ModelUtil.modelFromString(responseClass);
            if (!m.isEmpty()) {
                this.classLinkName = m.get()._1();
                this.className = responseClass;
            }else if (trueType != null) {
                this.className = responseClass;
                this.classLinkName = trueType;
            }

            if (responseClass.equalsIgnoreCase("void")) {
                this.className = null;
            } else {
                this.className = responseClass;
            }

        }


    }

    public LinkedList<MustacheResponseClass> getGenericClasses() {
        return genericClasses;
    }

    public void setGenericClasses(LinkedList<MustacheResponseClass> genericClasses) {
        this.genericClasses = genericClasses;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getClassLinkName() {
        return classLinkName;
    }

    public void setClassLinkName(String classLinkName) {
        this.classLinkName = classLinkName;
    }
}

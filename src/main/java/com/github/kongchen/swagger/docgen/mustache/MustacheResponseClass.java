package com.github.kongchen.swagger.docgen.mustache;

import java.util.LinkedList;

import com.github.kongchen.swagger.docgen.TypeUtils;

public class MustacheResponseClass {
    private String responseClass;
    private String responseClassLinkType;
    private LinkedList<MustacheResponseClass> genericClasses;

    public MustacheResponseClass(String responseClass) {
        if ((TypeUtils.genericPattern.matcher(responseClass).matches())) {
            String trueType = TypeUtils.getTrueType(responseClass);
            this.responseClassLinkType = trueType;
            this.responseClass = trueType;
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
            if (trueType != null) {
                this.responseClass = responseClass;
                this.responseClassLinkType = trueType;
            } else {
                if (responseClass.equalsIgnoreCase("void")) {
                    this.responseClass = null;
                } else {
                    this.responseClass = responseClass;
                }
            }
        }


    }

    String getResponseClass() {
        return responseClass;
    }

    void setResponseClass(String responseClass) {
        this.responseClass = responseClass;
    }

    String getResponseClassLinkType() {
        return responseClassLinkType;
    }

    void setResponseClassLinkType(String responseClassLinkType) {
        this.responseClassLinkType = responseClassLinkType;
    }

    public LinkedList<MustacheResponseClass> getGenericClasses() {
        return genericClasses;
    }

    public void setGenericClasses(LinkedList<MustacheResponseClass> genericClasses) {
        this.genericClasses = genericClasses;
    }
}

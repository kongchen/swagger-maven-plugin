package com.github.kongchen.swagger.docgen.mustache;

import java.util.List;
import java.util.Map;

public class MustacheParameterSet {
    private String paramType;

    private List<MustacheParameter> paras;

    public MustacheParameterSet(Map.Entry<String, List<MustacheParameter>> entry) {
        this.paramType = entry.getKey();
        this.paras = entry.getValue();
    }

    public String getParamType() {
        return paramType;
    }

    public void setParamType(String paramType) {
        this.paramType = paramType;
    }

    public List<MustacheParameter> getParas() {
        return paras;
    }

    public void setParas(List<MustacheParameter> paras) {
        this.paras = paras;
    }
}

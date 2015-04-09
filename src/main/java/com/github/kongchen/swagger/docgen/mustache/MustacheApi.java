package com.github.kongchen.swagger.docgen.mustache;

import com.github.kongchen.swagger.docgen.util.Utils;
import com.wordnik.swagger.model.ApiDescription;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class MustacheApi {
    private final String description;

    private String path;

    private final String url;

    private int apiIndex;

    private final List<MustacheOperation> operations = new LinkedList<MustacheOperation>();

    public MustacheApi(String basePath, ApiDescription api) {
        this.path = api.path();
        if (this.path != null && !this.path.startsWith("/")) {
            this.path = "/" + this.path;
        }
        this.url = basePath + api.path();
        this.description = Utils.getStrInOption(api.description());
    }

    public void addOperation(MustacheOperation operation) {
        operations.add(operation);
    }

    public int getApiIndex() {
        return apiIndex;
    }

    public void setApiIndex(int apiIndex) {
        this.apiIndex = apiIndex;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getUrl() {
        return url;
    }

    public List<MustacheOperation> getOperations() {
        return operations;
    }

    public String getDescription() {
        return description;
    }

    public void resetOperationPositions() {
        List<Integer> ops = new ArrayList<Integer>(getOperations().size());
        for(int i = 0; i < getOperations().size(); i++) {
            ops.add(i, null);
        }

        //sort op first
        Collections.sort(getOperations(), new Comparator<MustacheOperation>() {
            @Override
            public int compare(MustacheOperation o1, MustacheOperation o2) {
                return o1.getOpIndex() - o2.getOpIndex();
            }
        });

        Iterator<MustacheOperation> it = getOperations().iterator();
        int count = 1;
        while (it.hasNext()) {
            MustacheOperation op = it.next();
            op.setOpIndex(count++);
        }

    }
}

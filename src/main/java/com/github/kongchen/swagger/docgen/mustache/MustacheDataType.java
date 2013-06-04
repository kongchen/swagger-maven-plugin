package com.github.kongchen.swagger.docgen.mustache;

import java.util.List;

public class MustacheDataType implements Comparable<MustacheDataType> {

    String name;

    List<MustacheItem> items;

    public MustacheDataType(MustacheDocument mustacheDocument, String requestType) {
        this.name = requestType;
        this.items = mustacheDocument.analyzeDataTypes(requestType);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MustacheDataType)) return false;

        MustacheDataType that = (MustacheDataType) o;

        if (!name.equals(that.name)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (items != null ? items.hashCode() : 0);
        return result;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<MustacheItem> getItems() {
        return items;
    }

    public void setItems(List<MustacheItem> items) {
        this.items = items;
    }

    @Override
    public int compareTo(MustacheDataType o) {
        if (o == null) {
            return 1;
        }
        return this.name.compareTo(o.getName());
    }

    @Override
    public String toString() {
        return "MustacheDataType{" +
                "name='" + name + '\'' +
                ", items=" + items +
                '}';
    }
}

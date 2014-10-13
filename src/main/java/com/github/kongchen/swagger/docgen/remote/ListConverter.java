package com.github.kongchen.swagger.docgen.remote;

import com.github.kongchen.swagger.docgen.remote.model.CanBeSwaggerModel;
import scala.collection.JavaConversions;

import java.util.ArrayList;
import java.util.List;

/**
 * Convert from java.util.List<T> to scala.collection.immutable.List<R>
 *
* Created by kongchen on 14/10/11.
*/
public class ListConverter<T extends CanBeSwaggerModel<R>, R> {
    private List<T> sources;

    public ListConverter(List<T> sources) {
        this.sources = sources;
    }

    public scala.collection.immutable.List<R> convert() {
        if (sources == null || sources.size() == 0) return null;
        List<R> result = new ArrayList<R>();
        for (T ja : sources) {
            R a = ja.toSwaggerModel();
            if (a != null) {
                result.add(a);
            }
        }
        if (result.size() == 0) {
            return null;
        }
        return scala.collection.immutable.List.fromIterator(JavaConversions.asScalaIterator(result.iterator()));
    }
}

package com.github.kongchen.swagger.docgen.remote.model;

import com.wordnik.swagger.model.AllowableRangeValues;
import com.wordnik.swagger.model.AllowableValues;

/**
 * Created by chekong on 10/11/14.
 */
public class JAllowableRangeValues extends JAllowableValues{
    private String min;
    private String max;

    public void setMin(String min) {
        this.min = min;
    }

    public void setMax(String max) {
        this.max = max;
    }

    public String getMin() {
        return min;
    }

    public String getMax() {
        return max;
    }

    @Override
    public AllowableRangeValues toSwaggerModel() {
        return new AllowableRangeValues(min, max);
    }
}

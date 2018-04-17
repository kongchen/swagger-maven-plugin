/**
 * Copyright 2014 Reverb Technologies, Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wordnik.sample;

import java.text.SimpleDateFormat;
import java.util.Date;

public class JavaRestResourceUtil {
    public int getInt(final int minVal, final int maxVal, final int defaultValue, final String inputString) {
        int output;
        try {
            output = Integer.parseInt(inputString);
        } catch (final Exception e) {
            output = defaultValue;
        }

        if (output < minVal) {
            output = minVal;
        }
        if (maxVal == -1) {
            if (output < minVal) {
                output = minVal;
            }
        } else if (output > maxVal) {
            output = maxVal;
        }
        return output;
    }

    public long getLong(final long minVal, final long maxVal, final long defaultValue, final String inputString) {
        long output;
        try {
            output = Long.parseLong(inputString);
        } catch (final Exception e) {
            output = defaultValue;
        }

        if (output < minVal) {
            output = minVal;
        }
        if (maxVal == -1) {
            if (output < minVal) {
                output = minVal;
            }
        } else if (output > maxVal) {
            output = maxVal;
        }
        return output;
    }

    public double getDouble(final double minVal, final double maxVal, final double defaultValue, final String inputString) {
        double output;
        try {
            output = Double.parseDouble(inputString);
        } catch (final Exception e) {
            output = defaultValue;
        }

        if (output < minVal) {
            output = minVal;
        }
        if (maxVal == -1) {
            if (output < minVal) {
                output = minVal;
            }
        } else if (output > maxVal) {
            output = maxVal;
        }
        return output;
    }

    public boolean getBoolean(final boolean defaultValue, final String booleanString) {
        boolean output;

        //  treat "", "YES" as "true"
        if ("".equals(booleanString) || "YES".equalsIgnoreCase(booleanString)) {
            output = true;
        } else if ("NO".equalsIgnoreCase(booleanString)) {
            output = false;
        } else {
            try {
                output = Boolean.parseBoolean(booleanString);
            } catch (final Exception e) {
                output = defaultValue;
            }
        }
        return output;
    }

    public Date getDate(final Date defaultValue, final String dateString) {
        try {
            return new SimpleDateFormat("yyyy-MM-dd").parse(dateString);
        } catch (final Exception e) {
            return defaultValue;
        }
    }
}

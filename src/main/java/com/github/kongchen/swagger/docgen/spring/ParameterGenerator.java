package com.github.kongchen.swagger.docgen.spring;

import com.github.kongchen.swagger.docgen.util.LogAdapter;
import com.google.common.base.CharMatcher;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.core.ApiValues;
import com.wordnik.swagger.core.SwaggerSpec;
import com.wordnik.swagger.model.AllowableListValues;
import com.wordnik.swagger.model.AllowableRangeValues;
import com.wordnik.swagger.model.AllowableValues;
import com.wordnik.swagger.model.Parameter;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import scala.Option;
import scala.collection.JavaConversions;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class ParameterGenerator {

    private static final Pattern RANGE_VALUE_PATTERN = Pattern.compile("\\[(.+),(.+)\\]");
    private final LogAdapter logger;

    ParameterGenerator(LogAdapter logger) {
        this.logger = logger;
    }

    Parameter generateParameter(ParameterMetadata parameterMetadata) {
        AllowableValues allowed = null;
        String type = "";
        String name = "";
        boolean required = true;
        String description = "";

        for (Annotation annotation : parameterMetadata.getAnnotations()) { //loops through annotations for each parameter
            if (annotation.annotationType().equals(PathVariable.class)) {
                PathVariable pathVariable = (PathVariable) annotation;
                name = pathVariable.value();
                type = ApiValues.TYPE_PATH();
            } else if (annotation.annotationType().equals(RequestBody.class)) {
                RequestBody requestBody = (RequestBody) annotation;
                type = ApiValues.TYPE_BODY();
                required = requestBody.required();
            } else if (annotation.annotationType().equals(RequestParam.class)) {
                RequestParam requestParam = (RequestParam) annotation;
                name = requestParam.value();
                type = ApiValues.TYPE_QUERY();
                required = requestParam.required();
            } else if (annotation.annotationType().equals(RequestHeader.class)) {
                RequestHeader requestHeader = (RequestHeader) annotation;
                name = requestHeader.value();
                type = ApiValues.TYPE_HEADER();
                required = requestHeader.required();
            } else if (annotation.annotationType().equals(ApiParam.class)) {
                ApiParam apiParam = (ApiParam) annotation;
                if (apiParam.value() != null) {
                    description = apiParam.value();
                }
                try {
                    allowed = getAllowableValues(apiParam);
                } catch (Exception e) {
                    logger.error("error getting allowable values from @ApiParam", e);
                }
            }
        }

        String dataType = getParameterDataType(parameterMetadata.getType());
        return new Parameter(name, Option.apply(description), Option.<String>empty(), required, false, dataType,
                allowed, type, Option.<String>empty());
    }

    private static String getParameterDataType(Class<?> parameterClass) {
        String dataType;
        if (parameterClass.isArray()) {
            String clazzName = CharMatcher.anyOf("[]").removeFrom(parameterClass.getSimpleName());
            clazzName = generateTypeString(clazzName);
            dataType = "Array[" + clazzName + "]";
        } else {
            dataType = generateTypeString(parameterClass.getSimpleName());
        }
        return dataType;
    }

    private static AllowableValues getAllowableValues(ApiParam apiParam) {
        if (apiParam.allowableValues() != null) {
            if (apiParam.allowableValues().startsWith("range")) {
                return createRangeAllowedValues(apiParam);
            } else {
                return createAllowedValueList(apiParam);
            }
        }
        return null;
    }

    private static AllowableValues createAllowedValueList(ApiParam apiParam) {
        List<String> allowableValuesList;
        String allowableValues = CharMatcher.anyOf("[] ").removeFrom(apiParam.allowableValues());
        if (!(allowableValues.equals(""))) {
            allowableValuesList = Arrays.asList(allowableValues.split(","));
            return new AllowableListValues(
                    scala.collection.immutable.List.fromIterator(JavaConversions.asScalaIterator(allowableValuesList.iterator())), "LIST");
        } else {
            return null;
        }
    }

    private static AllowableValues createRangeAllowedValues(ApiParam apiParam) {
        String range = apiParam.allowableValues().substring("range".length());
        Matcher matcher = RANGE_VALUE_PATTERN.matcher(range);
        if (matcher.matches()) {
            String min = matcher.group(1);
            String max = matcher.group(2);
            return new AllowableRangeValues(min, max);
        } else {
            return null;
        }
    }

    private static String generateTypeString(String clazzName) {
        String typeString = clazzName;
        if (SwaggerSpec.baseTypes().contains(clazzName.toLowerCase())) {
            typeString = clazzName.toLowerCase();
        }
        return typeString;
    }
}

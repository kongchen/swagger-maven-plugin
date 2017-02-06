package com.github.kongchen.swagger.docgen.spring;

import com.github.kongchen.swagger.docgen.util.LogAdapter;
import com.google.common.collect.ImmutableSet;
import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;
import com.wordnik.swagger.converter.OverrideConverter;
import com.wordnik.swagger.core.SwaggerSpec;
import com.wordnik.swagger.model.AllowableListValues;
import com.wordnik.swagger.model.Model;
import com.wordnik.swagger.model.ModelProperty;
import com.wordnik.swagger.model.ModelRef;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.springframework.http.ResponseEntity;
import scala.Option;
import scala.collection.JavaConversions;
import scala.collection.mutable.LinkedHashMap;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ModelCollector {

    private final OverrideConverter overriderConverter;
    private Map<String, Model> models = new HashMap<String, Model>();

    private static final Option<String> DEFAULT_OPTION = Option.empty();
    private static final Set<String> RESERVED_PACKAGES = ImmutableSet.of("java", "org.springframework");
    private final LogAdapter logger;

    public ModelCollector(OverrideConverter overriderConverter, LogAdapter logger) {
        this.overriderConverter = overriderConverter;
        this.logger = logger;
    }

    public void addModel(Class<?> clazz) {
        String modelName = clazz.getSimpleName();
        if (isModel(clazz) && !(models.containsKey(modelName))) {
            //put the key first in models to avoid stackoverflow
            models.put(modelName, new Model(null, null, null, null, null, null, null, null));
            scala.collection.mutable.HashMap<String, Option<Model>> overriderMap = this.overriderConverter.overrides();
            if (overriderMap.contains(clazz.getCanonicalName())) {
                Option<Option<Model>> m = overriderMap.get(clazz.getCanonicalName());
                models.put(modelName, m.get().get());
            } else {
                models.put(modelName, generateModel(clazz));
            }
        }
    }

    private Model generateModel(Class<?> clazz) {
        logger.debug("generating model " + clazz);
        String modelDescription = "";
        LinkedHashMap<String, ModelProperty> modelProps = new LinkedHashMap<String, ModelProperty>();
        List<String> subTypes = new ArrayList<String>();

        if (clazz.isAnnotationPresent(ApiModel.class)) {
            ApiModel apiModel = clazz.getAnnotation(ApiModel.class);
            if (apiModel.description() != null) {
                modelDescription = apiModel.description();
            }
        }

        //<--Model properties from fields-->
        int x = 0;

        for (Field field : sortFields(clazz)) {
            //Only use fields if they are annotated - otherwise use methods
            boolean required = false;
            String description = "";
            ModelRef modelRef = null;
            Class<?> fieldType = field.getType();
            if (!(fieldType.equals(clazz))) {
                //if the types are the same, model will already be generated
                modelRef = generateModelRef(clazz, field); //recursive IFF there is a generic sub-type to be modeled
            }
            if (field.isAnnotationPresent(XmlElement.class) ||
                    field.isAnnotationPresent(ApiModelProperty.class)) {
                if (field.getAnnotation(XmlElement.class) != null) {
                    XmlElement xmlElement = field.getAnnotation(XmlElement.class);
                    required = xmlElement.required();
                }
                if (field.getAnnotation(ApiModelProperty.class) != null) {
                    ApiModelProperty amp = field.getAnnotation(ApiModelProperty.class);
                    if (!required) { //if required has already been changed to true, it was noted in XmlElement
                        required = amp.required();
                    }
                    description = amp.value();
                }

                if (!(fieldType.equals(clazz))) {
                    String name = field.getName();
                    addModel(fieldType);
                    subTypes.add(fieldType.getSimpleName());
                    logger.debug("adding model property from field " + name);
                    modelProps.put(name, generateModelProperty(fieldType, x, required, modelRef, description));
                    x++;
                }
            }
        }

        //<--Model properties from methods-->
        int i = 0;
        for (Method m : sortMethodsGettersFirst(clazz)) {
            boolean required = false;
            String description = "";
            ApiModelProperty amp;
            //look for required field in XmlElement annotation
            ModelRef modelRef = null;
            if (!(m.getReturnType().equals(clazz))) {
                modelRef = generateModelRef(clazz, m); //recursive IFF there is a generic sub-type to be modeled
            }

            if (m.isAnnotationPresent(XmlElement.class)) {
                XmlElement xmlElement = m.getAnnotation(XmlElement.class);
                required = xmlElement.required();
            } else if (m.isAnnotationPresent(JsonIgnore.class)
                    || m.isAnnotationPresent(XmlTransient.class)) {
                continue; //ignored fields
            }

            if (m.getAnnotation(ApiModelProperty.class) != null) {
                amp = m.getAnnotation(ApiModelProperty.class);
                required = amp.required();
                description = amp.value();
            }

            //get model properties from methods
            if (isGetterMethod(m)) {
                String name = getPropertyNameFromMethodName(m);
                if (!(m.getReturnType().equals(clazz))) {
                    addModel(m.getReturnType()); //recursive
                }
                if (!modelProps.contains(name)) {
                    logger.debug("adding model property " + name + " from method " + m);
                    modelProps.put(name, generateModelProperty(m.getReturnType(), i, required, modelRef, description));
                }
            }
            i++;
        }

        return new Model(clazz.getSimpleName(), clazz.getSimpleName(), clazz.getCanonicalName(), modelProps,
                Option.apply(modelDescription), DEFAULT_OPTION, DEFAULT_OPTION,
                scala.collection.immutable.List.fromIterator(JavaConversions.asScalaIterator(subTypes.iterator())));
    }

    private static boolean isModel(Class<?> clazz) {
        return !isInReservedPackage(clazz) && !SwaggerSpec.baseTypes().contains(clazz.getSimpleName().toLowerCase());
    }

    private static boolean isInReservedPackage(Class<?> clazz) {
        Package aPackage = clazz.getPackage();
        return aPackage != null && isReservedPackageName(aPackage.getName());
    }

    private static boolean isReservedPackageName(String packageName) {
        for (String str : RESERVED_PACKAGES) {
            if (packageName.contains(str)) {
                return true;
            }
        }
        return false;
    }

    private static Field[] sortFields(Class<?> clazz) {
        Field[] sortedFields = clazz.getDeclaredFields();
        Arrays.sort(sortedFields, new Comparator<Field>() {
            @Override
            public int compare(Field o1, Field o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        return sortedFields;
    }

    private static Method[] sortMethodsGettersFirst(Class<?> clazz) {
        Method[] ms = clazz.getMethods();
        Arrays.sort(ms, new Comparator<Method>() {
            @Override
            public int compare(Method m1, Method m2) {
                boolean m1IsGetter = isGetterMethod(m1);
                boolean m2IsGetter = isGetterMethod(m2);
                if (m1IsGetter) {
                    if (m2IsGetter) {
                        return getPropertyNameFromMethodName(m1).compareTo(getPropertyNameFromMethodName(m2));
                    } else {
                        return 1;
                    }
                } else {
                    if (!m2IsGetter) {
                        return m1.getName().compareTo(m2.getName());
                    } else {
                        return -1;
                    }
                }
            }
        });
        return ms;
    }

    private static String getPropertyNameFromMethodName(Method method) {
        String stripPrefix;
        String methodName = method.getName();
        if (methodName.startsWith("get") && methodName.length() > 3) {
            stripPrefix = methodName.substring(3);
        } else if (methodName.startsWith("is") && methodName.length() > 2) {
            stripPrefix = methodName.substring(2);
        } else {
            stripPrefix = methodName;
        }
        return lowerCamelCase(stripPrefix);
    }

    private static String lowerCamelCase(String name) {
        if (name.isEmpty()) {
            return name;
        }
        String lowerFirstLetter = name.substring(0, 1).toLowerCase(); //convert to camel case
        if (name.length() > 1) {
            return lowerFirstLetter + name.substring(1);
        } else {
            return lowerFirstLetter;
        }
    }

    private static boolean isGetterMethod(Method m) {
        String name = m.getName();
        return (name.startsWith("get") || name.startsWith("is")) && !(name.equals("getClass"));
    }

    private static ModelProperty generateModelProperty(Class<?> clazz, int position, boolean required, ModelRef modelRef,
                                                       String description) {
        AllowableListValues allowed = null;
        String name = clazz.getSimpleName();

        if (!(isModel(clazz))) {
            name = name.toLowerCase();
        }
        //check for enumerated values - currently strings only
        //TODO: support ranges
        if (clazz.isEnum()) {
            List<String> enums = new ArrayList<String>();
            for (Object obj : clazz.getEnumConstants()) {
                enums.add(obj.toString());
            }
            allowed = new AllowableListValues(
                    scala.collection.immutable.List.fromIterator(JavaConversions.asScalaIterator(enums.iterator())), "LIST");
        }

        return new ModelProperty(name,
                clazz.getCanonicalName(), position, required, Option.apply(description), allowed,
                Option.apply(modelRef));
    }

    private ModelRef generateModelRef(Class<?> clazz, Method m) {
        ModelRef modelRef = null; //can be null
        if (Collection.class.isAssignableFrom(m.getReturnType()) || m.getReturnType().equals(ResponseEntity.class)
                || m.getReturnType().equals(JAXBElement.class)) {
            Class<?> c = SpringReaderUtils.getGenericSubtype(m.getReturnType(), m.getGenericReturnType());
            String modelName = c.getSimpleName();
            if (isModel(c) && !(c.equals(clazz))) {
                addModel(c);
                modelRef = new ModelRef(modelName, Option.apply(modelName), Option.apply(modelName));
            } else {
                modelRef = new ModelRef(modelName.toLowerCase(), DEFAULT_OPTION, DEFAULT_OPTION);
            }
        }
        return modelRef;
    }

    private ModelRef generateModelRef(Class<?> clazz, Field f) {
        ModelRef modelRef = null;
        if (Collection.class.isAssignableFrom(f.getType()) || f.getType().equals(ResponseEntity.class)
                || f.getType().equals(JAXBElement.class)) {
            Class<?> c = SpringReaderUtils.getGenericSubtype(f.getType(), f.getGenericType());
            String modelName = c.getSimpleName();
            if (isModel(c) && !(c.equals(clazz))) {
                addModel(c);
                modelRef = new ModelRef(modelName, Option.apply(modelName), Option.apply(modelName));
            } else {
                modelRef = new ModelRef(modelName.toLowerCase(), DEFAULT_OPTION, DEFAULT_OPTION);
            }
        }
        return modelRef;
    }

    public Map<String, Model> getModels() {
        return models;
    }
}

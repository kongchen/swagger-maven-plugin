package com.github.kongchen.swagger.docgen.reader;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.kongchen.swagger.docgen.GenerateException;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.converter.ModelConverter;
import io.swagger.converter.ModelConverterContext;
import io.swagger.jackson.ModelResolver;
import io.swagger.models.Model;
import io.swagger.models.properties.Property;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.springframework.core.annotation.AnnotationUtils;

/**
 * Created by chekong on 15/5/19.
 */
public class ModelModifier extends ModelResolver {
    private Map<Type, Type> modelSubtitutes = new HashMap<Type, Type>();

    public ModelModifier(ObjectMapper mapper) {
        super(mapper);
    }

    public void addModelSubstitute(String fromClass, String toClass) throws GenerateException {
        try {
            Type type = _mapper.constructType(Class.forName(fromClass));
            Type toType = _mapper.constructType(Class.forName(toClass));

            modelSubtitutes.put(type, toType);

        } catch (ClassNotFoundException e) {
            throw new GenerateException(e);
        }
    }
    
    List<String> apiModelPropertyAccessExclusions = new ArrayList<String>();

    public List<String> getApiModelPropertyAccessExclusions() {
        return apiModelPropertyAccessExclusions;
    }

    public void setApiModelPropertyAccessExclusions(List<String> apiModelPropertyAccessExclusions) {
        this.apiModelPropertyAccessExclusions = apiModelPropertyAccessExclusions;
    }
    
    @Override
    public Property resolveProperty(Type type, ModelConverterContext context, Annotation[] annotations, Iterator<ModelConverter> chain) {
        if(modelSubtitutes.containsKey(type)) {
            return super.resolveProperty(modelSubtitutes.get(type), context, annotations, chain);
        } else if(chain.hasNext()) {
            return chain.next().resolveProperty(type, context, annotations, chain);
        } else {
            return super.resolveProperty(type, context, annotations, chain);
        }

    }

    @Override
    public Model resolve(Type type, ModelConverterContext context, Iterator<ModelConverter> chain) {
        if(modelSubtitutes.containsKey(type)) {
            return super.resolve(modelSubtitutes.get(type), context, chain);
        } else {
            return super.resolve(type, context, chain);
        }
    }

    @Override
    public Model resolve(JavaType type, ModelConverterContext context, Iterator<ModelConverter> chain) {
        Model model = super.resolve(type, context, chain);
        
        // If there are no @ApiModelPropety exclusions configured, return the untouched model
        if (apiModelPropertyAccessExclusions == null || apiModelPropertyAccessExclusions.isEmpty()) {
            return model;
        } 
        
        Class<?> cls = type.getRawClass();
                
        for (Method method : cls.getDeclaredMethods()) {
            
            ApiModelProperty apiModelPropertyAnnotation = AnnotationUtils.findAnnotation(method, ApiModelProperty.class);
            
            if (false == (apiModelPropertyAnnotation instanceof ApiModelProperty)) {
                continue;
            }
            
            String apiModelPropertyAccess = apiModelPropertyAnnotation.access();
            String apiModelPropertyName = apiModelPropertyAnnotation.name();
            
            // If the @ApiModelProperty is not populated with both #name and #access, skip it
            if (apiModelPropertyAccess.isEmpty() || apiModelPropertyName.isEmpty()) {
                continue;
            }
            
            // Check to see if the value of @ApiModelProperty#access is one to exclude.
            // If so, remove it from the previously-calculated model.
            if (apiModelPropertyAccessExclusions.contains(apiModelPropertyAccess)) {
                model.getProperties().remove(apiModelPropertyName);
            }
        }
        
        return model;
    }
}

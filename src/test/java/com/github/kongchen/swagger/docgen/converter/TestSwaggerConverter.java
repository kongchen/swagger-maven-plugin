package com.github.kongchen.swagger.docgen.converter;

import com.wordnik.swagger.annotations.ApiModelProperty;
import com.wordnik.swagger.converter.SwaggerSchemaConverter;
import com.wordnik.swagger.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import scala.Option;
import scala.collection.immutable.Map;

import javax.xml.bind.annotation.XmlElement;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;

public class TestSwaggerConverter extends SwaggerSchemaConverter {

  private final Logger log = LoggerFactory.getLogger(TestSwaggerConverter.class);

  @Override
  public Option<Model> read(final Class<?> modelClass, final Map<String, String> typeMap) {
    final Option<Model> modelOption = super.read(modelClass, typeMap);
    Class<?> currentClass = modelClass;

    while (currentClass.getSuperclass() != null) {
      for (final Method method : modelClass.getDeclaredMethods()) {
        if (method.getName().equals("getNotes")) {
          hideModelProperty(currentClass, method, modelOption);
        }
      }
      currentClass = currentClass.getSuperclass();
    }
    return modelOption;
  }

  private void hideModelProperty(final Class<?> currentClass, final Method method, final Option<Model> modelOption) {
    final String propertyName;

    if (method.isAnnotationPresent(XmlElement.class) && !method.getAnnotation(XmlElement.class).name().isEmpty()) {
      propertyName = method.getAnnotation(XmlElement.class).name();
    } else {
      propertyName = getClassMemberName(currentClass, method);
    }

    if (StringUtils.hasText(propertyName)) {
      modelOption.get().properties().remove(propertyName);
      log.debug("Successfully hidden API model property '" + propertyName + "'");
    }
  }

  private String getClassMemberName(final Class<?> currentClass, final Method method) {
    try {
      for (PropertyDescriptor propertyDescriptor : Introspector.getBeanInfo(currentClass).getPropertyDescriptors()) {
        if (propertyDescriptor.getReadMethod().getName().equals(method.getName())) {
          return propertyDescriptor.getName();
        }
      }
    } catch (IntrospectionException e) {
      log.error("Unable to retrieve field name", e);
      e.printStackTrace();
    }

    return null;
  }
}
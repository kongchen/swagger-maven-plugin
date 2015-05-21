/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.kongchen.swagger.docgen.reader;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wordnik.swagger.converter.ModelConverter;
import com.wordnik.swagger.jackson.AbstractModelConverter;
import com.wordnik.swagger.models.properties.Property;

/**
 *
 * @author andrewbird
 */
public class ModelConverterHelper extends AbstractModelConverter implements ModelConverter {

    public ModelConverterHelper(ObjectMapper mapper) {
        super(mapper);
    }
    
    public Property getPropertyFromTypeName(String typeName){
        return this.getPrimitiveProperty(typeName);
    }
        
}

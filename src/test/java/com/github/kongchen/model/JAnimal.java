package com.github.kongchen.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.wordnik.swagger.annotations.ApiModelProperty;

import java.util.Date;

/**
 * Created by chekong on 15/3/14.
 */
@JsonTypeInfo( use=JsonTypeInfo.Id.NAME, include=JsonTypeInfo.As.PROPERTY, property="type" )
@JsonSubTypes( { @JsonSubTypes.Type( value = JWildAnimal.class, name = "wild" ),
        @JsonSubTypes.Type( value = JDomesticAnimal.class, name = "domestic" ) } )
public class JAnimal {
    private String type;
    private Date date;

    @ApiModelProperty(value = "type of animal", position = 1)
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @ApiModelProperty (value = "Date added to the zoo", position = 2)
    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
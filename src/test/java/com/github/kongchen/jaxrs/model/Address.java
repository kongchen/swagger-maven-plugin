package com.github.kongchen.jaxrs.model;

import com.wordnik.swagger.annotations.ApiModelProperty;

import javax.xml.bind.annotation.XmlElement;

public class Address implements Entity<Integer> {
    private Integer id;

    private String location;

    private Long zipCode;

    /**
     * {@inheritDoc}
     */
    @XmlElement(name = "id")
    @ApiModelProperty(value = "Address' indentifier", position = 2)
    @Override
    public Integer getId() {
        return id;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setId(Integer id) {
        this.id = id;
    }

    @XmlElement(name = "zip", required = false)
    @ApiModelProperty(value = "The zip code for this location", position = 3)
    public Long getZipCode() {
        return zipCode;
    }

    public void setZipCode(Long zipCode) {
        this.zipCode = zipCode;
    }

    @XmlElement(name = "location")
    @ApiModelProperty(value = "The location", required = false, position = 1)
    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}

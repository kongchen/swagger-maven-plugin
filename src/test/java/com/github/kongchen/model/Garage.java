package com.github.kongchen.model;

import com.wordnik.swagger.annotations.ApiModelProperty;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 *
 * @author: chekong
 * 05/14/2013
 */
public class Garage {
    @ApiModelProperty(required = true, value = "Garage's id")
    private int id;

    @ApiModelProperty(required = true, value = "The name of the garage")
    private String name;

    @ApiModelProperty(required = true, value = "The description of the garage")
    private String description;

    @ApiModelProperty(required = true, value = "The address of the garage")
    private String address;

    @ApiModelProperty(required = true, value = "The telephone of the garage")
    private String telephone;

    @ApiModelProperty(required = false, value = "Holding cars")
    private List<com.github.kongchen.model.v2.Car> holdingCars;

    public List<com.github.kongchen.model.v2.Car> getHoldingCars() {
        return holdingCars;
    }

    public void setHoldingCars(List<com.github.kongchen.model.v2.Car> holdingCars) {
        this.holdingCars = holdingCars;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }
}

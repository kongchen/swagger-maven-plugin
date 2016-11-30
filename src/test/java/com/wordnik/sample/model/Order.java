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

package com.wordnik.sample.model;

import com.google.common.base.Optional; // Must be Google Guava Optional, because we run Java 6.
import io.swagger.annotations.ApiModelProperty;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;

@XmlRootElement(name = "Order")
public class Order {
    private long id;
    private long petId;
    private int quantity;
    private Date shipDate;
    private String status;
    private boolean complete;
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private Optional<String> optionalStatus;

    private String internalThing;
    private String anotherInternalThing;

    @XmlElement(name = "id")
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public boolean isComplete() {
        return complete;
    }

    public void setComplete(boolean complete) {
        this.complete = complete;
    }

    @XmlElement(name = "petId")
    public long getPetId() {
        return petId;
    }

    public void setPetId(long petId) {
        this.petId = petId;
    }

    @XmlElement(name = "quantity")
    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    @XmlElement(name = "status")
    @ApiModelProperty(value = "Order Status", allowableValues = "placed, approved, delivered")
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @XmlElement(name = "shipDate")
    public Date getShipDate() {
        return shipDate;
    }

    public void setShipDate(Date shipDate) {
        this.shipDate = shipDate;
    }

    @XmlElement(name = "optionalStatus")
    public Optional<String> getOptionalStatus() { return optionalStatus; }

    public void setOptionalStatus(@SuppressWarnings("OptionalUsedAsFieldOrParameterType") Optional<String> optionalStatus) { this.optionalStatus = optionalStatus; }

    @ApiModelProperty(name = "internalThing", access = "secret-property")
    public String getInternalThing() {
        return internalThing;
    }

    @ApiModelProperty(name = "anotherInternalThing", access = "another-secret-property")
    public String getAnotherInternalThing() {
        return anotherInternalThing;
    }

}

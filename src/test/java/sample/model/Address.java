package sample.model;

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
    @ApiModelProperty(value = "Address' indentifier")
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
    @ApiModelProperty(value = "The location")
    public Long getZipCode() {
        return zipCode;
    }

    public void setZipCode(Long zipCode) {
        this.zipCode = zipCode;
    }

    @XmlElement(name = "location")
    @ApiModelProperty(value = "The zip code for this location", required = false)
    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}

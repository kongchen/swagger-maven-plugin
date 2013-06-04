package sample.model;

import com.wordnik.swagger.annotations.ApiProperty;

import javax.xml.bind.annotation.XmlElement;

public class Address implements Entity<Integer> {
    private Integer id;

    private String location;

    private Long zipCode;

    /**
     * {@inheritDoc}
     */
    @XmlElement(name = "id")
    @ApiProperty(notes = "Address' indentifier")
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
    @ApiProperty(notes = "The location")
    public Long getZipCode() {
        return zipCode;
    }

    public void setZipCode(Long zipCode) {
        this.zipCode = zipCode;
    }

    @XmlElement(name = "location")
    @ApiProperty(notes = "The zip code for this location", required = false)
    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}

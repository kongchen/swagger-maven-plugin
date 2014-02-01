package sample.model;

import com.wordnik.swagger.annotations.ApiModelProperty;

import javax.xml.bind.annotation.XmlElement;

public class Email implements Entity<Integer> {
    private String email;

    private Boolean byDefault;

    private Integer id;

    /**
     * {@inheritDoc}
     */
    @XmlElement(name = "id")
    @ApiModelProperty(value = "Email identifier")
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

    @XmlElement(name = "address")
    @ApiModelProperty(value = "Email address")
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @XmlElement(name = "default", required = false)
    @ApiModelProperty(value = "The flag shows if this email is a default email",
            required = false)
    public Boolean getByDefault() {
        return byDefault;
    }

    public void setByDefault(Boolean byDefault) {
        this.byDefault = byDefault;
    }
}

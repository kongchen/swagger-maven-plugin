package sample.model;

import com.wordnik.swagger.annotations.ApiProperty;

import javax.xml.bind.annotation.XmlElement;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: kongchen
 * Date: 6/4/13
 */
public class Customer implements Entity<Integer>{
    private String name;

    private String surname;

    private Integer id;

    private Address address;

    private List<Email> emails;

    /**
     * {@inheritDoc}
     */
    @XmlElement(name = "id")
    @ApiProperty(required = true, notes = "Customer's identifier")
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

    @XmlElement(name = "surname")
    @ApiProperty(required = true, notes = "Customer's surname")
    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    @XmlElement(name = "name")
    @ApiProperty(required = true, notes = "Customer's name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @XmlElement(required = false, name = "email")
    @ApiProperty(notes = "The list of customer emails", required = false)
    public List<Email> getEmails() {
        return emails;
    }

    public void setEmails(List<Email> emails) {
        this.emails = emails;
    }

    @XmlElement(name = "address", required = false)
    @ApiProperty(required = false, notes = "Customer's address")
    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }
}


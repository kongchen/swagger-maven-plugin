package sample.model;

import java.util.List;

import com.wordnik.swagger.annotations.ApiProperty;

/**
 * Created with IntelliJ IDEA.
 *
 * @author: chekong
 * 05/14/2013
 */
public class Garage {
    @ApiProperty(required = true, notes = "Garage's id")
    private int id;

    @ApiProperty(required = true, notes = "The name of the garage")
    private String name;

    @ApiProperty(required = true, notes = "The description of the garage")
    private String description;

    @ApiProperty(required = true, notes = "The address of the garage")
    private String address;

    @ApiProperty(required = true, notes = "The telephone of the garage")
    private String telephone;

    @ApiProperty(required = false, notes = "Holding cars")
    private List<Car> holdingCars;

    public List<Car> getHoldingCars() {
        return holdingCars;
    }

    public void setHoldingCars(List<Car> holdingCars) {
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

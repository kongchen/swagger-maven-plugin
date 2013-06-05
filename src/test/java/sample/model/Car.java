package sample.model;

import java.util.List;

import com.wordnik.swagger.annotations.ApiProperty;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created with IntelliJ IDEA.
 *
 * @author: chekong
 * 05/14/2013
 */
//@XmlRootElement(name = "car")
public class Car {
    @ApiProperty(required = true, notes = "car's id")
    private int id;

    @ApiProperty(required = true, notes = "The brand of the car")
    private String brand;

    @ApiProperty(required = true, notes = "The engine parameter of the car")
    private String engine;

    @ApiProperty(required = true, notes = "Indicate the car is MT or AT")
    private boolean mt;

    @ApiProperty(required = true, notes = "The price of the car", access = "readOnly")
    private float price;

    @ApiProperty(required = false, notes = "Any other comments for the car")
    private String notes;

    @ApiProperty(required = false, notes = "Repair history")
    private List<String> repairHistory;

    public List<String> getRepairHistory() {
        return repairHistory;
    }

    public void setRepairHistory(List<String> repairHistory) {
        this.repairHistory = repairHistory;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getEngine() {
        return engine;
    }

    public void setEngine(String engine) {
        this.engine = engine;
    }

    public boolean isMt() {
        return mt;
    }

    public void setMt(boolean mt) {
        this.mt = mt;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}

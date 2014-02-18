package sample.model;

import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 *
 * @author: chekong
 * 05/14/2013
 */
@ApiModel(value = "car")
public class Car {
    @ApiModelProperty(required = true, value = "car's id")
    private int id;

    @ApiModelProperty(required = true, value = "The brand of the car", allowableValues = "Ford,Toyota, Kia")
    private String brand;

    @ApiModelProperty(required = true, value = "The engine parameter of the car")
    private String engine;

    @ApiModelProperty(required = true, value = "Indicate the car is MT or AT")
    private boolean mt;

    @ApiModelProperty(required = true, value = "The price of the car", access = "readOnly")
    private float price;

    @ApiModelProperty(required = false, value = "Any other comments for the car")
    private String notes;

    @ApiModelProperty(required = false, value = "Repair history")
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

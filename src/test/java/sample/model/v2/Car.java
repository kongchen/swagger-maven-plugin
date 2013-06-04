package sample.model.v2;

import com.wordnik.swagger.annotations.ApiProperty;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created with IntelliJ IDEA.
 *
 * @author: chekong
 * 05/14/2013
 */
@XmlRootElement(name = "nEwcar")
public class Car {
    @ApiProperty(required = true, notes = "car's id")
    private int id;

    @ApiProperty(required = true, notes = "The brand of the car")
    private String brand;

    @ApiProperty(required = true, notes = "The engine parameter of the car")
    private String engine;

    @ApiProperty(required = true, notes = "Indicate the car is MT or AT")
    private boolean mt;

    @ApiProperty(required = false, notes = "Any other comments for the car")
    private String notes;

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

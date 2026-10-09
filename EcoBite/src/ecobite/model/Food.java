package ecobite.model;

import java.io.Serializable;

public class Food implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum FoodType {
        HUMAN,
        ANIMAL
    }

    public enum FoodStatus {
        AVAILABLE,
        PENDING,
        APPROVED,
        COMPLETED,
        REJECTED,
        TIME_FINISHED,
        ANIMAL_REVIEW
    }

    private int id;
    private String foodName;
    private int quantity;
    private FoodType type;
    private String preparedTime;
    private String expiryTime = "N/A";
    private FoodStatus status;

    public Food() {
    }

    public Food(int id, String foodName, int quantity, FoodType type, String preparedTime, FoodStatus status) {
        this(id, foodName, quantity, type, preparedTime, "N/A", status);
    }

    public Food(int id, String foodName, int quantity, FoodType type, String preparedTime, String expiryTime,
                FoodStatus status) {
        this.id = id;
        this.foodName = foodName;
        this.quantity = quantity;
        this.type = type;
        this.preparedTime = preparedTime;
        this.expiryTime = expiryTime;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFoodName() {
        return foodName;
    }

    public void setFoodName(String foodName) {
        this.foodName = foodName;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public FoodType getType() {
        return type;
    }

    public void setType(FoodType type) {
        this.type = type;
    }

    public String getPreparedTime() {
        return preparedTime;
    }

    public void setPreparedTime(String preparedTime) {
        this.preparedTime = preparedTime;
    }

    public String getExpiryTime() {
        return expiryTime == null || expiryTime.isBlank() ? "N/A" : expiryTime;
    }

    public void setExpiryTime(String expiryTime) {
        this.expiryTime = expiryTime;
    }

    public FoodStatus getStatus() {
        return status;
    }

    public void setStatus(FoodStatus status) {
        this.status = status;
    }
}

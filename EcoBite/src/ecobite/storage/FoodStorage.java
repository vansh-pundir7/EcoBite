package ecobite.storage;

import ecobite.model.Food;

public class FoodStorage extends DataStorage<Food> {
    public FoodStorage() {
        super("data");
    }

    public static final String FILE_NAME = "foods.ser";
}

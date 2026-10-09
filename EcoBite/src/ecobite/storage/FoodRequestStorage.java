package ecobite.storage;

import ecobite.model.FoodRequest;

public class FoodRequestStorage extends DataStorage<FoodRequest> {
    public FoodRequestStorage() {
        super("data");
    }

    public static final String FILE_NAME = "food_requests.ser";
}

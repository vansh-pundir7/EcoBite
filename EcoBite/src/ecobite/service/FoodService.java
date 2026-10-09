package ecobite.service;

import ecobite.model.Food;
import ecobite.storage.FoodStorage;

import java.util.ArrayList;
import java.util.List;

public class FoodService {
    private final FoodStorage storage;

    public FoodService() {
        this.storage = new FoodStorage();
    }

    public void addFood(Food food) {
        List<Food> foods = storage.loadList(FoodStorage.FILE_NAME);
        foods.add(food);
        storage.saveList(FoodStorage.FILE_NAME, foods);

        Food savedFood = findFoodById(food.getId());
        if (savedFood == null || !food.getFoodName().equals(savedFood.getFoodName())) {
            throw new IllegalStateException("Food was written but could not be loaded back from storage.");
        }
    }

    public List<Food> getAllFoods() {
        return storage.loadList(FoodStorage.FILE_NAME);
    }

    public Food findFoodById(int id) {
        for (Food food : getAllFoods()) {
            if (food.getId() == id) {
                return food;
            }
        }
        return null;
    }

    public void updateFoodStatus(int id, Food.FoodStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Food status is required.");
        }

        List<Food> foods = storage.loadList(FoodStorage.FILE_NAME);
        Food target = null;
        for (Food food : foods) {
            if (food.getId() == id) {
                target = food;
                break;
            }
        }
        if (target == null) {
            throw new IllegalArgumentException("Food record " + id + " was not found.");
        }

        target.setStatus(status);
        storage.saveList(FoodStorage.FILE_NAME, foods);
        if (findFoodById(id).getStatus() != status) {
            throw new IllegalStateException("Food status update could not be verified.");
        }
    }

    public List<Food> getAvailableFoods() {
        List<Food> availableFoods = new ArrayList<>();
        for (Food food : getAllFoods()) {
            if (food.getStatus() == Food.FoodStatus.AVAILABLE) {
                availableFoods.add(food);
            }
        }
        return availableFoods;
    }
}

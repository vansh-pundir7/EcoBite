package ecobite.service;

import ecobite.model.Food;

import javax.swing.Timer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

public class TimerService {
    private static final int CHECK_INTERVAL_MILLISECONDS = 60_000;
    private static final DateTimeFormatter EXPIRY_FORMATTER =
            DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm").withResolverStyle(ResolverStyle.STRICT);

    private final FoodService foodService;
    private Timer timer;

    public TimerService() {
        foodService = new FoodService();
    }

    public void start() {
        if (timer != null && timer.isRunning()) {
            return;
        }

        runExpiryCheck();
        timer = new Timer(CHECK_INTERVAL_MILLISECONDS, event -> runExpiryCheck());
        timer.setCoalesce(true);
        timer.start();
    }

    public void stop() {
        if (timer != null) {
            timer.stop();
        }
    }

    public void checkExpiry() {
        LocalDateTime now = LocalDateTime.now();
        for (Food food : foodService.getAllFoods()) {
            LocalDateTime expiryTime = parseExpiryTime(food.getExpiryTime());
            if (expiryTime == null || now.isBefore(expiryTime)) {
                continue;
            }

            Food.FoodStatus nextStatus = getNextStatus(food);
            if (nextStatus != null) {
                foodService.updateFoodStatus(food.getId(), nextStatus);
            }
        }
    }

    private void runExpiryCheck() {
        try {
            checkExpiry();
        } catch (RuntimeException error) {
            error.printStackTrace();
        }
    }

    private LocalDateTime parseExpiryTime(String value) {
        if (value == null || value.isBlank() || "N/A".equalsIgnoreCase(value.trim())) {
            return null;
        }

        try {
            return LocalDateTime.parse(value, EXPIRY_FORMATTER);
        } catch (DateTimeParseException error) {
            return null;
        }
    }

    private Food.FoodStatus getNextStatus(Food food) {
        if (food.getType() == Food.FoodType.HUMAN) {
            if (food.getStatus() == Food.FoodStatus.AVAILABLE) {
                return Food.FoodStatus.TIME_FINISHED;
            }
            if (food.getStatus() == Food.FoodStatus.TIME_FINISHED) {
                return Food.FoodStatus.ANIMAL_REVIEW;
            }
        } else if (food.getType() == Food.FoodType.ANIMAL
                && food.getStatus() == Food.FoodStatus.AVAILABLE) {
            return Food.FoodStatus.TIME_FINISHED;
        }
        return null;
    }
}
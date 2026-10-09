package ecobite.service;

import ecobite.model.User;
import ecobite.storage.UserStorage;

import java.util.ArrayList;
import java.util.List;

public class UserService {
    private final UserStorage storage;

    public UserService() {
        this.storage = new UserStorage();
    }

    public void addUser(User user) {
        List<User> users = storage.loadList(UserStorage.FILE_NAME);
        users.add(user);
        storage.saveList(UserStorage.FILE_NAME, users);
    }

    public List<User> getAllUsers() {
        return storage.loadList(UserStorage.FILE_NAME);
    }

    public boolean validateLogin(String username, String password) {
        for (User user : getAllUsers()) {
            if (user.getUsername().equals(username) && user.getPassword().equals(password)) {
                return true;
            }
        }
        return false;
    }

    public User findByUsername(String username) {
        for (User user : getAllUsers()) {
            if (user.getUsername().equals(username)) {
                return user;
            }
        }
        return null;
    }

    public List<User> getSampleUsers() {
        List<User> sampleUsers = new ArrayList<>();
        sampleUsers.add(new User("admin", "admin123", "System Admin"));
        sampleUsers.add(new User("ngo", "ngo123", "NGO Staff"));
        return sampleUsers;
    }
}

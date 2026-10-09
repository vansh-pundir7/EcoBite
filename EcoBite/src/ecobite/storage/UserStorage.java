package ecobite.storage;

import ecobite.model.User;

public class UserStorage extends DataStorage<User> {
    public UserStorage() {
        super("data");
    }

    public static final String FILE_NAME = "users.ser";
}

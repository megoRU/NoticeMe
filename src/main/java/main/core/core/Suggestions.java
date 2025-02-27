package main.core.core;

import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

public class Suggestions {

    private final Set<String> userList = new ConcurrentSkipListSet<>();

    public void putUser(String userId) {
        userList.add(userId);
    }

    public void removeUser(String userId) {
        userList.remove(userId);
    }

    public boolean containsUser(String userId) {
        return userList.contains(userId);
    }

    public Set<String> getUserList() {
        return Set.copyOf(userList);
    }
}
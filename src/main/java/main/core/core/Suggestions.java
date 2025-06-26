package main.core.core;

import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

public class Suggestions {

    private final Set<Long> userList = new ConcurrentSkipListSet<>();

    public void putUser(Long userId) {
        userList.add(userId);
    }

    public void removeUser(Long userId) {
        userList.remove(userId);
    }

    public boolean containsUser(Long userId) {
        return userList.contains(userId);
    }

    public Set<Long> getUserList() {
        return Set.copyOf(userList);
    }
}
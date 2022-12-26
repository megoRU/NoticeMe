package main.core;


import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

public class TrackingUser {

    private final Set<String> userList = new ConcurrentSkipListSet<>();

    public void putUser(String userId) {
        userList.add(userId);
    }

    public String getUserList() {
        StringBuilder stringBuilder = new StringBuilder();
        userList.forEach(u -> {
            if (stringBuilder.isEmpty()) {
                stringBuilder.append("<@").append(u).append(">");
            } else {
                stringBuilder.append(", <@").append(u).append(">");
            }
        });
        return stringBuilder.toString();
    }

    public void removeUserFromList(String userId) {
        userList.remove(userId);
    }
}
package main.core.core;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

public class TrackingUser {

    private final Set<String> userList = new ConcurrentSkipListSet<>();
    private LocalDateTime timeJoin = null;

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

    public boolean hasUserJoin() {
        if (timeJoin == null) {
            setTimeJoin();
            return false;
        } else if (LocalDateTime.now().isAfter(timeJoin)) {
            timeJoin = null;
            return false;
        } else {
            return true;
        }
    }

    public Set<String> getUserListSet() {
        return Set.copyOf(userList);
    }

    public int getUserCount() {
        return userList.size();
    }

    public void putUser(String userId) {
        userList.add(userId);
    }

    public void removeUserFromList(String userId) {
        userList.remove(userId);
    }

    private void setTimeJoin() {
        timeJoin = LocalDateTime.now().plusMinutes(10);
    }
}
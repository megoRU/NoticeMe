package main.core;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

public class TrackingUser {

    private final Set<String> userList = new ConcurrentSkipListSet<>();
    private LocalDateTime timeJoin = null;

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

    public int getUserCount() {
        return userList.size();
    }

    public void removeUserFromList(String userId) {
        userList.remove(userId);
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

    private void setTimeJoin() {
        timeJoin = LocalDateTime.now().plusMinutes(15);
    }

}
package main.core;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TrackingUser {

    private final Set<String> userList = new HashSet<>();

    public void putUser(String userId) {
        userList.add(userId);
    }

    public String getUserList() {
        StringBuilder stringBuilder = new StringBuilder();
        List<String> localList = new ArrayList<>(userList);

        for (String s : localList) {
            if (stringBuilder.isEmpty()) {
                stringBuilder.append("<@").append(s).append(">");
            } else {
                stringBuilder.append(", <@").append(s).append(">");
            }
        }
        return stringBuilder.toString();
    }

}
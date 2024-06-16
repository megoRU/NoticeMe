package main.core.core;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.User;

import java.util.List;

@AllArgsConstructor
@Getter
@Setter
@Builder
public class MessageData {

    private String userName;
    private String voiceChannelId;
    private String messageId;
    private String guildId;
    private List<User> userList;

    public boolean isUserHere(String userId, String guildId) {
        if (!this.guildId.equals(guildId)) return false;
        return userList.stream().anyMatch(user -> user.getId().equals(userId));
    }

    public void removeUserFromList(String userId) {
        userList = userList.stream().filter(user -> !user.getId().equals(userId)).toList();
    }

    public String getUserList() {
        StringBuilder stringBuilder = new StringBuilder();
        userList.forEach(u -> {
            if (stringBuilder.isEmpty()) {
                stringBuilder.append("<@").append(u.getId()).append(">");
            } else {
                stringBuilder.append(", <@").append(u.getId()).append(">");
            }
        });
        return stringBuilder.toString();
    }
}
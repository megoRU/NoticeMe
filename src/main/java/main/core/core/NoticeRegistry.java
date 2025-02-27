package main.core.core;

import main.model.entity.Server;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

public class NoticeRegistry {

    //Guild | List: userTrackerId | TrackingUser
    private static final ConcurrentMap<String, ConcurrentMap<String, TrackingUser>> trackingUserConcurrentMap = new ConcurrentHashMap<>();
    private static final ConcurrentMap<String, Server> serverListMap = new ConcurrentHashMap<>();
    //Guild | List: user | SuggestionsList
    private static final ConcurrentMap<String, Set<String>> userSuggestionsMap = new ConcurrentHashMap<>();
    private static volatile NoticeRegistry noticeRegistry;

    private NoticeRegistry() {
    }

    public static NoticeRegistry getInstance() {
        if (noticeRegistry == null) {
            synchronized (NoticeRegistry.class) {
                if (noticeRegistry == null) {
                    noticeRegistry = new NoticeRegistry();
                }
            }
        }
        return noticeRegistry;
    }

    public Set<String> getAllUserTrackerIdsByUserId(String guildId, String referenceId) {
        return trackingUserConcurrentMap.getOrDefault(guildId, new ConcurrentHashMap<>())
                .entrySet()
                .stream()
                .filter(entry -> entry.getValue().getUserListSet().contains(referenceId))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    private void save(String guildId, ConcurrentMap<String, TrackingUser> concurrentMap) {
        trackingUserConcurrentMap.put(guildId, concurrentMap);
    }

    /*
        userId этот тот кто подписан на userIdTracker
     */
    public void sub(String guildId, String userId, String userIdTracker) {
        if (!hasGuild(guildId)) {
            TrackingUser trackingUser = new TrackingUser();
            trackingUser.putUser(userId);
            ConcurrentMap<String, TrackingUser> trackingUserConcurrentMap = new ConcurrentHashMap<>();
            trackingUserConcurrentMap.put(userIdTracker, trackingUser);
            save(guildId, trackingUserConcurrentMap);
        } else {
            TrackingUser trackingUserFromMap = getUser(guildId, userIdTracker);
            if (trackingUserFromMap == null) {
                TrackingUser trackingUser = new TrackingUser();
                trackingUser.putUser(userId);
                saveTrackingUser(guildId, userIdTracker, trackingUser);
            } else {
                get(guildId).get(userIdTracker).putUser(userId);
            }
        }
    }

    public void addUserSuggestions(String userId, String userSuggestionId) {
        Set<String> stringSet = userSuggestionsMap.get(userId);
        if (stringSet != null) {
            stringSet.add(userSuggestionId);
        } else {
            stringSet = new HashSet<>();
            stringSet.add(userSuggestionId);
            userSuggestionsMap.put(userId, stringSet);
        }
    }

    public Set<String> getSuggestions(String userId) {
        Set<String> strings = userSuggestionsMap.get(userId);
        if (strings != null) return strings;
        else {
            userSuggestionsMap.put(userId, new HashSet<>());
            return userSuggestionsMap.get(userId);
        }
    }

    //                                                        TrackingUser | Data
    private void saveTrackingUser(String guildId, String user, TrackingUser trackingUser) {
        ConcurrentMap<String, TrackingUser> stringTrackingUserConcurrentMap = trackingUserConcurrentMap.get(guildId);
        if (stringTrackingUserConcurrentMap == null)
            throw new IllegalArgumentException("In this collection does not exist Guild: saveTrackingUser()");
        stringTrackingUserConcurrentMap.put(user, trackingUser);
    }

    //TrackingUser | Data
    private ConcurrentMap<String, TrackingUser> get(String guildId) {
        return trackingUserConcurrentMap.get(guildId);
    }

    public void putServer(String serverId, Server server) {
        serverListMap.put(serverId, server);
    }

    public void removeServer(String serverId) {
        serverListMap.remove(serverId);
    }

    @Nullable
    public Server getServer(String serverId) {
        return serverListMap.get(serverId);
    }

    @Nullable
    public TrackingUser getUser(String guildId, String user) {
        ConcurrentMap<String, TrackingUser> stringTrackingUserConcurrentMap = trackingUserConcurrentMap.get(guildId);
        if (stringTrackingUserConcurrentMap == null) return null;
        return stringTrackingUserConcurrentMap.get(user);
    }

    private boolean hasGuild(String guildId) {
        return trackingUserConcurrentMap.containsKey(guildId);
    }

    public void removeGuild(String guildId) {
        trackingUserConcurrentMap.remove(guildId);
    }

    private void removeUserFromGuild(String guildId, String user) {
        trackingUserConcurrentMap.get(guildId).remove(user);
    }

    public void unsub(String guildId, String trackingUserId, String userId) {
        ConcurrentMap<String, TrackingUser> stringTrackingUserConcurrentMap = trackingUserConcurrentMap.get(guildId);
        if (stringTrackingUserConcurrentMap != null) {
            TrackingUser trackingUser = stringTrackingUserConcurrentMap.get(trackingUserId);
            if (trackingUser != null) trackingUser.removeUserFromList(userId);
        }
    }

    public void removeUserFromAllGuild(String user) {
        ConcurrentMap<String, ConcurrentMap<String, TrackingUser>> listUsers = new ConcurrentHashMap<>(trackingUserConcurrentMap);
        listUsers.forEach((guild, trackerUser) -> {
            TrackingUser trackingUser = trackerUser.get(user);
            if (trackingUser != null) {
                removeUserFromGuild(guild, user);
            }
        });
    }
}
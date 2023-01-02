package main.core;

import javax.annotation.Nullable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class NoticeRegistry {

    //Guild | List: userTrackerId | TrackingUser
    private static final ConcurrentMap<String, ConcurrentMap<String, TrackingUser>> trackingUserConcurrentMap = new ConcurrentHashMap<>();
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

    private void save(String guildId, ConcurrentMap<String, TrackingUser> concurrentMap) {
        trackingUserConcurrentMap.put(guildId, concurrentMap);
    }

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

    //                                                        TrackingUser | Data
    private void saveTrackingUser(String guildId, String user, TrackingUser trackingUser) {
        ConcurrentMap<String, TrackingUser> stringTrackingUserConcurrentMap = trackingUserConcurrentMap.get(guildId);
        if (stringTrackingUserConcurrentMap == null) throw new IllegalArgumentException("In this collection does not exist Guild: saveTrackingUser()");
        stringTrackingUserConcurrentMap.put(user, trackingUser);
    }

    //TrackingUser | Data
    private ConcurrentMap<String, TrackingUser> get(String guildId) {
        return trackingUserConcurrentMap.get(guildId);
    }

    @Nullable
    public TrackingUser getUser(String guildId, String user) {
        ConcurrentMap<String, TrackingUser> stringTrackingUserConcurrentMap = trackingUserConcurrentMap.get(guildId);
        if (stringTrackingUserConcurrentMap == null) return null;
        return stringTrackingUserConcurrentMap.get(user);
    }

    public boolean hasGuild(String guildId) {
        return trackingUserConcurrentMap.containsKey(guildId);
    }

    public void removeGuild(String guildId) {
        trackingUserConcurrentMap.remove(guildId);
    }

    public void removeUserFromGuild(String guildId, String user) {
        trackingUserConcurrentMap.get(guildId).remove(user);
    }

    public void unsub(String guildId, String trackingUserId, String userId) {
        ConcurrentMap<String, TrackingUser> stringTrackingUserConcurrentMap = trackingUserConcurrentMap.get(guildId);
        if (stringTrackingUserConcurrentMap != null) {
            stringTrackingUserConcurrentMap.get(trackingUserId).removeUserFromList(userId);
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
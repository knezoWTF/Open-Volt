package com.volt.utils.friend;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class FriendManager {
    private static final Set<UUID> friends = new HashSet<>();

    public static void addFriend(UUID uuid) {
        friends.add(uuid);
    }

    public static void removeFriend(UUID uuid) {
        friends.remove(uuid);
    }

    public static boolean isFriend(UUID uuid) {
        return friends.contains(uuid);
    }

    public static void toggleFriend(UUID uuid) {
        if (isFriend(uuid)) {
            removeFriend(uuid);
        } else {
            addFriend(uuid);
        }
    }

    public static Set<UUID> getFriends() {
        return new HashSet<>(friends);
    }

    public static void clearFriends() {
        friends.clear();
    }
}
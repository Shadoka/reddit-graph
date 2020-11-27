package com.shadoka.graph.outbound;

import java.util.List;
import java.util.Map;

public class FriendMapping {

    private Map<String, List<String>> userToFriend;

    public Map<String, List<String>> getUserToFriend() {
        return userToFriend;
    }

    public void setUserToFriend(Map<String, List<String>> userToFriend) {
        this.userToFriend = userToFriend;
    }
}

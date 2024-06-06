package com.example.dolt.users;

public class User{
    private String username, userId, fcmToken;
    private Boolean isFriend, isFriendRequest;
    public User(String username, String userId, Boolean isFriend, Boolean isFriendRequest) {
        this.username = username;
        this.userId = userId;
        this.isFriend = isFriend;
        this.isFriendRequest = isFriendRequest;
    }

    public String getUserId() {
        return userId;
    }

    public Boolean getFriend() {
        return isFriend;
    }

    public void setFriend(Boolean friend) {
        isFriend = friend;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFcmToken() {
        return fcmToken;
    }

    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    public void setFriendRequest(Boolean friendRequest) {
        isFriendRequest = friendRequest;
    }

    public Boolean getFriendRequest() {
        return isFriendRequest;
    }
}

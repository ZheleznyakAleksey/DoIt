package com.example.dolt.users;

public class User {
    private String username, userId, fcmToken, userImage;
    private Boolean isFriend, isFriendRequest;
    private int id;

    public User(String username, String userId, Boolean isFriend, Boolean isFriendRequest) {
        this.username = username;
        this.userId = userId;
        this.isFriend = isFriend;
        this.isFriendRequest = isFriendRequest;
    }

    public User(String username, String userId) {
        this.username = username;
        this.userId = userId;
    }

    public User(String username, String userId, String userImage, Boolean isFriend, Boolean isFriendRequest) {
        this.username = username;
        this.userId = userId;
        this.userImage = userImage;
        this.isFriend = isFriend;
        this.isFriendRequest = isFriendRequest;
    }

    public User() {

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

    public String getUserImage() {
        return userImage;
    }

    public void setUserImage(String userImage) {
        this.userImage = userImage;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}

package com.dexter.mystyle.models;

public class User {

    private String userId;
    private String userName;
    private String email;
    private String phoneNumber;
    private Boolean isSeller;

    public User() {}

    public User(String userId, String userName, String email, Boolean isSeller) {
        this.userId = userId;
        this.userName = userName;
        this.email = email;
        this.phoneNumber = "";
        this.isSeller = isSeller;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Boolean getSeller() {
        return isSeller;
    }

    public void setSeller(Boolean seller) {
        isSeller = seller;
    }
}

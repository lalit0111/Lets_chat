package com.example.letschat;

public class userDetails {

    String phoneNumber;
    String username;
    String imageUrl;
    String status;


    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }



    public userDetails(){
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getUsername() {
        return username;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public userDetails(String username, String phoneNumber, String imageUrl,String status) {
        this.phoneNumber = phoneNumber;
        this.username = username;
        this.imageUrl = imageUrl;
        this.status=status;
    }
}

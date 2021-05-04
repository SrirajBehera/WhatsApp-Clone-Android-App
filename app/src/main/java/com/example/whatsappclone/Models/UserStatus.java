package com.example.whatsappclone.Models;

import java.util.ArrayList;

public class UserStatus {

    private String name, profilePic;
    private long lastUpdated;
    private ArrayList<Status> statuses;

    public UserStatus(String name, String profilePic, long lastUpdated, ArrayList<Status> statuses) {
        this.name = name;
        this.profilePic = profilePic;
        this.lastUpdated = lastUpdated;
        this.statuses = statuses;
    }

    public UserStatus() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public ArrayList<Status> getStatuses() {
        return statuses;
    }

    public void setStatuses(ArrayList<Status> statuses) {
        this.statuses = statuses;
    }
}

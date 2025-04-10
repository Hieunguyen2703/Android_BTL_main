package com.lapTrinhUUDD.movie.Models;

import java.util.UUID;

public class Message {
    private String id = UUID.randomUUID().toString();
    private String content;
    private String time;
    private int role;

    public Message(String content, String time, int role) {
        this.content = content;
        this.time = time;
        this.role = role;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public int getRole() {
        return role;
    }

    public void setRole(int role) {
        this.role = role;
    }
}

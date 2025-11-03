package com.tuca.model;


import lombok.Data;

@Data
public class Task {

    private int id;
    private String ownerName;
    private String description;
    private long startDate;
    private long expiryDate;
    private String status;

    public Task(String description, String ownerName, long expiryDate) {
        this.description = description;
        this.startDate = System.currentTimeMillis();
        this.expiryDate = expiryDate;
        this.ownerName = ownerName;
        this.status = "Pendente";
    }

    public Task(int id, String description, String ownerName, long startDate, long expiryDate, String status) {
        this.id = id;
        this.description = description;
        this.ownerName = ownerName;
        this.startDate = startDate;
        this.expiryDate = expiryDate;
        this.status = status;
    }

}

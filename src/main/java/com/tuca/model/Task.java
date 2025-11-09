package com.tuca.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Data
@Entity
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String ownerName;
    private String description;
    private long startDate;
    private long expiryDate;
    private String status;

    public Task(String description, String ownerName, long expiryDate) {
        this.description = description;
        this.ownerName = ownerName;
        this.expiryDate = expiryDate;
        this.startDate = System.currentTimeMillis();
        this.status = "Pendente";
    }

    public Task() {

    }
}

package com.tuca.manager;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class QueueManager {

    private List<String> queue = new ArrayList<>();

    public void add(String consumerId) {
        queue.add(consumerId);
    }

    public void remove(String consumerId) {
        queue.remove(consumerId);
    }


}

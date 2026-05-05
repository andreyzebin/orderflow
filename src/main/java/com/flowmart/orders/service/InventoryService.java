package com.flowmart.orders.service;

import com.flowmart.orders.dto.InventoryItem;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class InventoryService {

    private final Map<Long, InventoryItem> store = new HashMap<>();

    public InventoryItem lookup(Long itemId) {
        return store.get(itemId);
    }

    public void register(InventoryItem item) {
        store.put(item.getId(), item);
    }

    public void decrement(Long itemId, int qty) {
        InventoryItem item = store.get(itemId);
        item.setOnHand(item.getOnHand() - qty);
    }
}

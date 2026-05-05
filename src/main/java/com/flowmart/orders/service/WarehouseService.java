package com.flowmart.orders.service;

import com.flowmart.orders.dto.InventoryItem;
import com.flowmart.orders.dto.WarehouseReservation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WarehouseService {

    private final InventoryService inventoryService;

    public WarehouseReservation reserve(Long itemId, int qty) {
        InventoryItem item = inventoryService.lookup(itemId);
        String ref = UUID.randomUUID().toString();
        inventoryService.decrement(itemId, qty);
        return new WarehouseReservation(itemId, qty, ref);
    }

    public void release(Long itemId, int qty) {
        InventoryItem item = inventoryService.lookup(itemId);
        item.setOnHand(item.getOnHand() + qty);
    }
}

package com.flowmart.orders.service;

import com.flowmart.orders.dto.InventoryItem;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class InventoryServiceTest {

    @Test
    void registerAndLookup_roundTrip() {
        InventoryService svc = new InventoryService();
        svc.register(InventoryItem.builder().id(7L).onHand(5).warehouseCode("MAIN").build());
        InventoryItem found = svc.lookup(7L);
        assertNotNull(found);
        assertEquals(5, found.getOnHand());
    }

    @Test
    void decrement_lowersOnHand() {
        InventoryService svc = new InventoryService();
        svc.register(InventoryItem.builder().id(7L).onHand(5).warehouseCode("MAIN").build());
        svc.decrement(7L, 2);
        assertEquals(3, svc.lookup(7L).getOnHand());
    }
}

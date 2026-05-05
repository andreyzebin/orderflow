package com.flowmart.orders.service;

import com.flowmart.orders.dto.InventoryItem;
import com.flowmart.orders.dto.WarehouseReservation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class WarehouseServiceTest {

    private InventoryService inventoryService;
    private WarehouseService warehouseService;

    @BeforeEach
    void setUp() {
        inventoryService = new InventoryService();
        warehouseService = new WarehouseService(inventoryService);
        inventoryService.register(InventoryItem.builder().id(1L).onHand(10).warehouseCode("MAIN").build());
    }

    @Test
    void reserve_returnsReservationAndDecrementsStock() {
        WarehouseReservation r = warehouseService.reserve(1L, 3);
        assertNotNull(r.reservationRef());
        assertEquals(7, inventoryService.lookup(1L).getOnHand());
    }

    @Test
    void release_addsBackToStock() {
        warehouseService.release(1L, 4);
        assertEquals(14, inventoryService.lookup(1L).getOnHand());
    }
}

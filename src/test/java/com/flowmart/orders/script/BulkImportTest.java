package com.flowmart.orders.script;

import com.flowmart.orders.dto.InventoryItem;
import com.flowmart.orders.dto.WarehouseReservation;
import com.flowmart.orders.model.OrderItem;
import com.flowmart.orders.service.InventoryService;
import com.flowmart.orders.service.WarehouseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BulkImportTest {

    private BulkImport bulkImport;
    private InventoryService inventoryService;

    @BeforeEach
    void setUp() {
        inventoryService = new InventoryService();
        bulkImport = new BulkImport(new WarehouseService(inventoryService));
        for (long id = 1; id <= 3; id++) {
            inventoryService.register(InventoryItem.builder().id(id).onHand(100).warehouseCode("MAIN").build());
        }
    }

    @Test
    void importItems_reservesEachItem() {
        List<OrderItem> items = new ArrayList<>();
        items.add(OrderItem.builder().id(1L).quantity(5).build());
        items.add(OrderItem.builder().id(2L).quantity(2).build());
        items.add(OrderItem.builder().id(3L).quantity(7).build());

        List<WarehouseReservation> rs = bulkImport.importItems(items);

        assertEquals(3, rs.size());
        assertEquals(95, inventoryService.lookup(1L).getOnHand());
        assertEquals(98, inventoryService.lookup(2L).getOnHand());
        assertEquals(93, inventoryService.lookup(3L).getOnHand());
    }
}

package com.flowmart.orders.script;

import com.flowmart.orders.dto.WarehouseReservation;
import com.flowmart.orders.model.OrderItem;
import com.flowmart.orders.service.WarehouseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class BulkImport {

    private final WarehouseService warehouseService;

    public List<WarehouseReservation> importItems(List<OrderItem> items) {
        List<WarehouseReservation> reservations = new ArrayList<>();
        for (OrderItem item : items) {
            WarehouseReservation r = warehouseService.reserve(item.getId(), item.getQuantity());
            reservations.add(r);
        }
        return reservations;
    }
}

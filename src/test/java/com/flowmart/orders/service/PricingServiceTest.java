package com.flowmart.orders.service;

import com.flowmart.orders.model.*;
import com.flowmart.orders.repository.OrderItemRepository;
import com.flowmart.orders.repository.OrderRepository;
import com.flowmart.orders.repository.PromotionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PricingServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private OrderItemRepository orderItemRepository;
    @Mock private PromotionRepository promotionRepository;

    @InjectMocks
    private PricingService pricingService;

    private Promotion promotion;
    private Product product;

    @BeforeEach
    void setUp() {
        product = Product.builder()
                .id(1L)
                .name("Widget")
                .category("electronics")
                .basePrice(new BigDecimal("10.00"))
                .build();

        promotion = new Promotion();
        promotion.setId(1L);
        promotion.setName("Buy 3 Get 1 Free - Electronics");
        promotion.setCategory("electronics");
        promotion.setBuyQuantity(3);
        promotion.setStartDate(LocalDate.now().minusDays(1));
        promotion.setEndDate(LocalDate.now().plusDays(30));

        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(orderItemRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void applyBulkDiscount_withFourItems_firstItemBecomeFree() {
        OrderItem item1 = buildItem(1L, new BigDecimal("10.00"));
        OrderItem item2 = buildItem(2L, new BigDecimal("10.00"));
        OrderItem item3 = buildItem(3L, new BigDecimal("10.00"));
        OrderItem item4 = buildItem(4L, new BigDecimal("10.00"));

        Order order = buildOrder(List.of(item1, item2, item3, item4));

        Order result = pricingService.applyBulkDiscount(order, promotion);

        assertThat(result.getDiscountAmount()).isEqualByComparingTo("10.00");
    }

    @Test
    void applyBulkDiscount_withNoEligibleItems_returnsUnchangedOrder() {
        Product otherProduct = Product.builder()
                .id(2L).name("Shirt").category("clothing")
                .basePrice(new BigDecimal("25.00")).build();

        OrderItem item = OrderItem.builder()
                .id(1L).product(otherProduct).quantity(5)
                .unitPrice(new BigDecimal("25.00"))
                .discountAmount(BigDecimal.ZERO).build();

        Order order = buildOrder(List.of(item));
        Order result = pricingService.applyBulkDiscount(order, promotion);

        assertThat(result.getDiscountAmount()).isEqualByComparingTo("0.00");
    }

    private OrderItem buildItem(Long id, BigDecimal price) {
        return OrderItem.builder()
                .id(id).product(product).quantity(1)
                .unitPrice(price).discountAmount(BigDecimal.ZERO)
                .build();
    }

    private Order buildOrder(List<OrderItem> items) {
        Order order = Order.builder()
                .id(1L)
                .customer(Customer.builder().id(1L).email("buyer@example.com").build())
                .status(OrderStatus.OPEN)
                .subtotal(items.stream()
                        .map(OrderItem::getUnitPrice)
                        .reduce(BigDecimal.ZERO, BigDecimal::add))
                .discountAmount(BigDecimal.ZERO)
                .taxAmount(new BigDecimal("8.00"))
                .total(new BigDecimal("48.00"))
                .build();
        items.forEach(i -> i.setOrder(order));
        order.getItems().addAll(items);
        return order;
    }
}

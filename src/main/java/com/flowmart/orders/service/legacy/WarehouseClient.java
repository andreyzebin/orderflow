package com.flowmart.orders.service.legacy;

/**
 * @deprecated replaced by {@link com.flowmart.orders.service.WarehouseService}.
 *             Kept on the classpath while the legacy bulk-import script is
 *             rewritten; will be removed when that migration completes.
 */
@Deprecated
public class WarehouseClient {

    public void reserveSync(Long itemId, int qty) {
        throw new UnsupportedOperationException("legacy stub");
    }
}

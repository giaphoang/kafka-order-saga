package dev.kafka.contracts;

public final class Topics {
    public static final String ORDER_CREATED = "commerce.order.created.v1";
    public static final String INVENTORY_RESULT = "commerce.inventory.result.v1";
    public static final String PAYMENT_RESULT = "commerce.payment.result.v1";
    public static final String ORDER_STATUS = "commerce.order.status.v1";
    private Topics() {}
}

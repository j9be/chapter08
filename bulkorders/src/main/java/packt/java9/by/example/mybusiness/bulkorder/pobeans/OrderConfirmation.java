package packt.java9.by.example.mybusiness.bulkorder.pobeans;

import packt.java9.by.example.mybusiness.bulkorder.pobeans.Order;

public class OrderConfirmation {
    private final Order order;
    private boolean isInconsistent;
    private boolean refused;

    public OrderConfirmation(Order order, boolean isInconsistent, boolean refused) {
        this.order = order;
        this.isInconsistent = isInconsistent;
        this.refused = refused;
    }

    public Order getOrder() {
        return order;
    }

    public boolean isInconsistent() {
        return isInconsistent;
    }

    public void setInconsistent(boolean inconsistent) {
        isInconsistent = inconsistent;
    }

    public boolean isRefused() {
        return refused;
    }

    public void setRefused(boolean refused) {
        this.refused = refused;
    }
}

package packt.java9.by.example.mybusiness.bulkorder.checkers;

import packt.java9.by.example.mybusiness.bulkorder.pobeans.Order;
import packt.java9.by.example.mybusiness.bulkorder.pobeans.OrderItem;

public class CheckHelper {
    final Order order;

    public CheckHelper(Order order) {
        this.order = order;
    }

    public boolean containsOneOf(String... ids) {
        for (final OrderItem item : order.getItems()) {
            for (final String id : ids) {
                if (item.getProductId().equals(id)) {
                    return true;
                }
            }
        }
        return false;
    }

}

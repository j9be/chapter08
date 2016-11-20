package packt.java9.by.example.mybusiness.bulkorder.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import packt.java9.by.example.mybusiness.bulkorder.pobeans.Order;
import packt.java9.by.example.mybusiness.bulkorder.pobeans.OrderConfirmation;
import packt.java9.by.example.mybusiness.bulkorder.services.Checker;

@RestController
public class OrderController {
    private Logger log = LoggerFactory.getLogger((OrderController.class));
    private final Checker checker;

    public OrderController(@Autowired Checker checker) {
        this.checker = checker;
    }

    @RequestMapping("/order")
    public OrderConfirmation getProductInformation(@RequestBody Order order) {
        if (checker.isConsistent(order)) {
            if (submitOrderIsOk(order)) {
                return oderOK(order);
            } else {
                return orderRefused(order);
            }
        } else {
            return orderInconsistent(order);
        }
    }


    private OrderConfirmation oderOK(Order order) {
        OrderConfirmation oc = new OrderConfirmation(order, false, false);
        return oc;
    }

    private OrderConfirmation orderRefused(Order order) {
        return new OrderConfirmation(order, false, true);
    }

    private OrderConfirmation orderInconsistent(Order order) {
        return new OrderConfirmation(order, true, false);
    }

    private boolean submitOrderIsOk(Order order) {
        return true;
    }
}

package packt.java9.by.example.mybusiness.bulkorder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.annotation.Annotation;
import java.util.*;

@RestController
public class OrderController {
    private Logger log = LoggerFactory.getLogger((OrderController.class));
    private final Collection<ConsistencyChecker> checkers;

    private final ProductLookup lookup;

    public OrderController(
            @Autowired Collection<ConsistencyChecker> checkers,
            @Autowired ProductLookup lookup) {
        this.checkers = checkers;
        this.lookup = lookup;
    }

    @RequestMapping("/order")
    public OrderConfirmation getProductInformation(@RequestBody Order order) {
        if (noConsistencyError(order)) {
            if (submitOrderIsOk(order)) {
                return oderOK(order);
            } else {
                return orderRefused(order);
            }
        } else {
            return orderInconsistent(order);
        }
    }

    private boolean noConsistencyError(Order order) {
        Map<OrderItem, ProductInformation> map = collectProductInformation(order);
        if (map == null) {
            return false;
        }
        Set<Class<? extends Annotation>> annotations = getProductAnnotations(order,map);
        for (ConsistencyChecker checker : checkers) {
            for (Annotation annotation : checker.getClass().getAnnotations()) {
                log.info("Looking at class {} if its annotation {} is in set {}", checker.getClass(), annotation.annotationType(), annotations);
                if (annotations.contains(annotation.annotationType())) {
                    if (checker.isInconsistent(order)) {
                        return false;
                    }
                    log.info("It does and now breaking");
                    break;
                }
                log.info("It does not");
            }
        }
        return true;
    }

    private Map<OrderItem, ProductInformation> collectProductInformation(Order order) {
        final Map<OrderItem, ProductInformation> map = new HashMap<>();
        for (OrderItem item : order.getItems()) {
            final ProductInformation pi = lookup.byId(item.getProductId());
            if (pi == ProductInformation.emptyProductInformation) {
                return null;
            }
            map.put(item, pi);
        }
        return map;
    }

    private Set<Class<? extends Annotation>> getProductAnnotations(Order order, Map<OrderItem, ProductInformation> productInformationMap) {
        final Set<Class<? extends Annotation>> annotations = new HashSet<>();
        for (OrderItem item : order.getItems()) {
            final ProductInformation pi = productInformationMap.get(item);
            if (pi.getCheck() != null) {
                for (Class<? extends Annotation> check : pi.getCheck()) {
                    log.info("Product {} is annotated with class {}", pi.getId(), pi.getCheck().get(0));
                    for (final Class<? extends Annotation> klass : pi.getCheck()) {
                        annotations.add(klass);
                    }
                }
            } else {
                log.info("Product {} has no annotation", pi.getId());
            }
        }
        return annotations;
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

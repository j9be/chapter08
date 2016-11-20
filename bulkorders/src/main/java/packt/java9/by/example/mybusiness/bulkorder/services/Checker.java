package packt.java9.by.example.mybusiness.bulkorder.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;
import packt.java9.by.example.mybusiness.bulkorder.ConsistencyChecker;
import packt.java9.by.example.mybusiness.bulkorder.dtos.Order;
import packt.java9.by.example.mybusiness.bulkorder.dtos.OrderItem;
import packt.java9.by.example.mybusiness.bulkorder.dtos.ProductInformation;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

@Component()
@RequestScope
public class Checker {
    Logger log = LoggerFactory.getLogger(Checker.class);

    private final Collection<ConsistencyChecker> checkers;
    private final ProductInformationCollector productInformationCollector;
    private final ProductsCheckerCollector productsCheckerCollector;

    public Checker(@Autowired Collection<ConsistencyChecker> checkers,
                   @Autowired ProductInformationCollector productInformationCollector,
                   @Autowired ProductsCheckerCollector productsCheckerCollector
    ) {
        this.checkers = checkers;
        this.productInformationCollector = productInformationCollector;
        this.productsCheckerCollector = productsCheckerCollector;
    }

    public boolean _isConsistent(Order order) {
        Map<OrderItem, ProductInformation> map = productInformationCollector.collectProductInformation(order);
        if (map == null) {
            return false;
        }
        Set<Class<? extends Annotation>> annotations = productsCheckerCollector.getProductAnnotations(order);
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

    /**
     * Check that amn order is consistent calling all consistency checkers that are relevant for the order.
     *
     * @param order
     * @return true if the order is consistent by all checkers
     */
    public boolean isConsistent(Order order) {
        Map<OrderItem, ProductInformation> map = productInformationCollector.collectProductInformation(order);
        if (map == null) {
            return false;
        }
        final Set<Class<? extends Annotation>> annotations = productsCheckerCollector.getProductAnnotations(order);
        return !checkers.stream().filter(checker ->
                Arrays.stream(checker.getClass().getAnnotations())
                        .filter(annotation -> annotations.contains(annotation.annotationType()))
                        .filter(x -> checker.isInconsistent(order))
                        .findAny()
                        .isPresent())
                .findAny()
                .isPresent();
    }


}

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
import java.util.function.Predicate;
import java.util.stream.IntStream;

@Component()
@RequestScope
public class Checker {
    private static final Logger log = LoggerFactory.getLogger(Checker.class);

    private final Collection<ConsistencyChecker> checkers;
    private final ProductInformationCollector piCollector;
    private final ProductsCheckerCollector pcCollector;

    public Checker(@Autowired Collection<ConsistencyChecker> checkers,
                   @Autowired ProductInformationCollector piCollector,
                   @Autowired ProductsCheckerCollector pcCollector
    ) {
        this.checkers = checkers;
        this.piCollector = piCollector;
        this.pcCollector = pcCollector;
    }

    public boolean isConsistent(Order order) {
        Map<OrderItem, ProductInformation> map =
                piCollector.collectProductInformation(order);
        if (map == null) {
            return false;
        }
        Set<Class<? extends Annotation>> annotations =
                pcCollector.getProductAnnotations(order);
        for (ConsistencyChecker checker :
                checkers) {
            for (Annotation annotation :
                    checker.getClass().getAnnotations()) {
                log.info("annotation {}", annotation);
                log.info("annotation class {}", annotation.getClass());
                Arrays.stream(annotation.getClass()
                        .getInterfaces()).forEach(
                        t -> log.info("annotation implemented interfaces {}", t)
                );

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
    public boolean _isConsistent(Order order) {
        Map<OrderItem, ProductInformation> map =
                piCollector.collectProductInformation(order);
        if (map == null) {
            return false;
        }
        final Set<Class<? extends Annotation>> annotations =
                pcCollector.getProductAnnotations(order);
        Predicate<Annotation> annotationIsNeeded = annotation ->
                annotations.contains(annotation.annotationType());
        Predicate<ConsistencyChecker> productIsConsistent = checker ->
                Arrays.stream(checker.getClass().getAnnotations())
                        .filter(annotationIsNeeded)
                        .anyMatch(x -> checker.isInconsistent(order));
        return !checkers.stream().anyMatch(productIsConsistent);
    }


}

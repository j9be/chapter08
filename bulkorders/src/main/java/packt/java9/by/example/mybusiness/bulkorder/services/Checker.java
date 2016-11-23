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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

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
                Arrays.stream(annotation.getClass().getInterfaces()[0].getInterfaces()).forEach(
                        t -> log.info("annotation implemented interfaces {}", t)
                );

                log.info("Looking at class {} if its annotation {} is in set {}", checker.getClass(), annotation.annotationType(), annotations);
                if (annotations.contains(annotation.annotationType())) {

                    if (isInconsistent(checker, order)) {
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

    private boolean isInconsistent(ConsistencyChecker checker, Order order) {
        final Method method = getSingleDeclaredPublicMethod(checker);
        if (method == null) {
            log.error(
                    "The checker {} has zero or more than one methods",
                    checker.getClass());
            return false;

        }
        final boolean inconsistent;
        try {
            inconsistent = (boolean) method.invoke(checker, order);
        } catch (InvocationTargetException |
                IllegalAccessException |
                ClassCastException e) {
            log.error("Calling the method {} on class {} threw exception",
                    method, checker.getClass());
            log.error("The exception is ", e);
            return false;
        }
        return inconsistent;
    }

    private Method getSingleDeclaredPublicMethod(ConsistencyChecker checker) {
        final Method[] methods = checker.getClass().getDeclaredMethods();
        Method singleMethod = null;
        for (Method method : methods) {
            if (Modifier.isPublic(method.getModifiers())) {
                if (singleMethod != null) {
                    return null;
                }
                singleMethod = method;
            }
        }
        return singleMethod;
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
        return !checkers.stream().filter(checker ->
                Arrays.stream(checker.getClass().getAnnotations())
                        .filter(annotation -> annotations.contains(annotation.annotationType()))
//                        .filter(x -> checker.isInconsistent(order))
                        .findAny()
                        .isPresent())
                .findAny()
                .isPresent();
    }


}

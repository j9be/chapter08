package packt.java9.by.example.mybusiness.bulkorder.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;
import packt.java9.by.example.mybusiness.bulkorder.pobeans.Order;
import packt.java9.by.example.mybusiness.bulkorder.pobeans.OrderItem;
import packt.java9.by.example.mybusiness.bulkorder.pobeans.ProductInformation;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequestScope
public class ProductsCheckerCollector {
    private static Logger log = LoggerFactory.getLogger(ProductsCheckerCollector.class);

    private final ProductInformationCollector pic;

    public ProductsCheckerCollector(@Autowired ProductInformationCollector pic) {
        this.pic = pic;
    }

    public Set<Class<? extends Annotation>> _getProductAnnotations(Order order) {
        Map<OrderItem, ProductInformation> piMap = pic.collectProductInformation(order);
        final Set<Class<? extends Annotation>> annotations = new HashSet<>();
        for (OrderItem item : order.getItems()) {
            final ProductInformation pi = piMap.get(item);
            if (pi != null && pi.getCheck() != null) {
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

    public Set<Class<? extends Annotation>> getProductAnnotations(Order order) {
        Map<OrderItem, ProductInformation> piMap = pic.collectProductInformation(order);
        return order.getItems().stream()
                .map(t -> piMap.get(t))
                .filter(pi -> pi != null)
                .peek(pi -> {
                    if (pi.getCheck() == null) {
                        log.info("Product {} has no annotation", pi.getId());
                    }
                })
                .filter(pi -> pi.getCheck() != null)
                .peek(pi -> log.info("Product {} is annotated with class {}", pi.getId(), pi.getCheck()))
                .flatMap(pi -> pi.getCheck().stream())
                .collect(Collectors.toSet());
    }
}

package packt.java9.by.example.mybusiness.bulkorder.checkers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import packt.java9.by.example.mybusiness.bulkorder.ConsistencyChecker;
import packt.java9.by.example.mybusiness.bulkorder.pobeans.Order;

@Component
@LampType
public class LampChecker implements ConsistencyChecker {
    Logger log = LoggerFactory.getLogger(LampChecker.class);

    @Override
    public boolean isInconsistent(Order order) {
        log.info("LampChecker checking order {}", order);
        CheckHelper helper = new CheckHelper(order);
        return !helper.containsOneOf("126", "127", "128");
    }
}

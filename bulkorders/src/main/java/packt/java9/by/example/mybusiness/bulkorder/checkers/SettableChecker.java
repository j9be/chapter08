package packt.java9.by.example.mybusiness.bulkorder.checkers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import packt.java9.by.example.mybusiness.bulkorder.ConsistencyChecker;
import packt.java9.by.example.mybusiness.bulkorder.dtos.Order;

@Component
@PoweredDevice
public class SettableChecker implements ConsistencyChecker {
    private static final Logger log = LoggerFactory.getLogger(SettableChecker.class);

    private boolean setValue = false;

    public boolean isInconsistent(Order order) {
        log.info("SettableChecker returns {}",setValue);
        return setValue;
    }
}

package packt.java9.by.example.mybusiness.bulkorder.checkers;

import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@Repeatable(PoweredDevices.class)
@Inherited
public @interface ParameteredPoweredDevice {
    String[] value();
}

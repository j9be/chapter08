package packt.java9.by.example.mybusiness.bulkorder.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import packt.java9.by.example.mybusiness.bulkorder.dtos.Order;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

@Component
public class CheckerScriptExecutor {
    private static final Logger log = LoggerFactory.getLogger(CheckerScriptExecutor.class);

    public boolean notConsistent(String script, Order order) {
        final ScriptEngineManager manager = new ScriptEngineManager();
        final ScriptEngine engine = manager.getEngineByName("JavaScript");
        final Reader scriptReader;
        try {
            final InputStream scriptIS = new ClassPathResource("scripts/" + script + ".js").getInputStream();
            scriptReader = new InputStreamReader(scriptIS);
        } catch (IOException ioe) {
            log.error("The script {} is not readable", script);
            log.error("Script opening exception", ioe);
            return true;
        }
        final Object result;
        try {
            engine.eval(scriptReader);
            Invocable inv = (Invocable) engine;
            result = inv.invokeFunction("isInconsistent", order);
        } catch (ScriptException|NoSuchMethodException se) {
            log.error("The script {} thruw up", script);
            log.error("Script executing exception", se);
            return true;
        }
        if (result instanceof Boolean) {
            return (boolean) result;
        }
        log.error("The script {} returned non boolean",script);
        if (result == null) {
            log.error("returned value is null");
        } else {
            log.error("returned type is {}", result.getClass());
        }
        return true;
    }

}

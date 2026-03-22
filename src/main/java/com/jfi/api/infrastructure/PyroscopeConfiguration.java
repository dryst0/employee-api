package com.jfi.api.infrastructure;

import io.pyroscope.http.Format;
import io.pyroscope.javaagent.PyroscopeAgent;
import io.pyroscope.javaagent.config.Config;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

@Configuration
@ConditionalOnProperty(name = "pyroscope.agent.enabled", havingValue = "true")
public class PyroscopeConfiguration {

    private static final Logger log = LogManager.getLogger(PyroscopeConfiguration.class);

    @Value("${pyroscope.application.name:employee-api}")
    private String applicationName;

    @Value("${pyroscope.server.address:http://localhost:4040}")
    private String serverAddress;

    @Value("${pyroscope.profiler.alloc:512k}")
    private String profilerAlloc;

    @Value("${pyroscope.profiler.lock:10ms}")
    private String profilerLock;

    @EventListener(ContextRefreshedEvent.class)
    public void start() {
        PyroscopeAgent.start(
            new Config.Builder()
                .setApplicationName(applicationName)
                .setServerAddress(serverAddress)
                .setProfilingAlloc(profilerAlloc)
                .setProfilingLock(profilerLock)
                .setFormat(Format.JFR)
                .build()
        );
        log.info("Pyroscope agent started: application={}, server={}", applicationName, serverAddress);
    }
}

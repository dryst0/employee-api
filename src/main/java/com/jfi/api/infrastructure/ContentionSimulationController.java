package com.jfi.api.infrastructure;

import java.util.concurrent.locks.ReentrantLock;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Profile("dev")
@RestController
public class ContentionSimulationController {

    private final ReentrantLock lock = new ReentrantLock();

    @GetMapping("/simulate/contention")
    public Mono<String> simulateContention(
        @RequestParam(defaultValue = "10") int threads,
        @RequestParam(defaultValue = "50") int holdMs
    ) {
        int clampedThreads = Math.min(threads, 50);
        int clampedHoldMs = Math.min(holdMs, 200);

        return Mono.fromCallable(() -> {
            Thread[] workers = new Thread[clampedThreads];
            for (int i = 0; i < clampedThreads; i++) {
                workers[i] = new Thread(() -> {
                    lock.lock();
                    try {
                        Thread.sleep(clampedHoldMs);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        lock.unlock();
                    }
                });
                workers[i].start();
            }
            for (Thread worker : workers) {
                worker.join();
            }
            return "Simulated lock contention: " + clampedThreads
                + " threads, " + clampedHoldMs + "ms hold time";
        }).subscribeOn(Schedulers.boundedElastic());
    }
}

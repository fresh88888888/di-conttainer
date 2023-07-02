package org.isp;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ScheduledUpdater {
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private long initialDelayInSeconds;
    private long periodInSeconds;
    private Updater updater;

    public ScheduledUpdater(long initialDelayInSeconds, long periodInSeconds, Updater updater) {
        this.initialDelayInSeconds = initialDelayInSeconds;
        this.periodInSeconds = periodInSeconds;
        this.updater = updater;
    }

    public void run(){
        executor.scheduleAtFixedRate(() -> updater.update(), periodInSeconds, initialDelayInSeconds, TimeUnit.SECONDS);
    }
}

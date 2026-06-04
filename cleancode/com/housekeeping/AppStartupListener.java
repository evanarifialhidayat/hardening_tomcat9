package com.housekeeping;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AppStartupListener implements ServletContextListener {

    private ScheduledExecutorService scheduler;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        String base = System.getProperty("catalina.base");
        scheduler = Executors.newSingleThreadScheduledExecutor();
        Runnable task = () -> {
            try {
                AppCleanTemp.clean(base + "/temp", 5000L);
				AppCleanTemp.clean(base + "/work", 5000L);
                System.out.println("[HOUSEKEEPING] " + new Date() + " cleanup done");
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
		task.run();
        scheduler.scheduleAtFixedRate(task,	0,5,TimeUnit.SECONDS);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (scheduler != null) {
            scheduler.shutdownNow();
        }
        System.out.println("[HOUSEKEEPING] stopped");
    }
}
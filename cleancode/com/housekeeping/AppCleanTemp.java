package com.housekeeping;

import java.io.File;

public class AppCleanTemp {

    public static void clean(String path, long maxAgeMillis) {
        File dir = new File(path);
        if (!dir.exists()) return;

        File[] files = dir.listFiles();
        if (files == null) return;

        long now = System.currentTimeMillis();

        for (File f : files) {
            if (now - f.lastModified() > maxAgeMillis) {
                delete(f);
            }
        }
    }

    private static void delete(File f) {
        if (f.isDirectory()) {
            for (File child : f.listFiles()) {
                delete(child);
            }
        }
        f.delete();
    }
}
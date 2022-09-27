package com.github.smvfal.placementresolver.utils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class TimeLogger {

    private final String[] HEADER = {"timestamp", "duration", "pods"};

    public void log(long time, int pods, String fileName) throws IOException {
        if (!Files.exists(Path.of(fileName)))
            writeToCsv(HEADER, fileName);

        long timestamp = System.currentTimeMillis() / 1000L;
        String[] fields = new String[]{Long.toString(timestamp), Long.toString(time), Integer.toString(pods)};
        writeToCsv(fields, fileName);
    }

    private void writeToCsv(String[] fields, String fileName) throws IOException {
        FileWriter fw = new FileWriter(fileName, true);
        BufferedWriter bw = new BufferedWriter(fw);
        bw.write(String.join(",", fields));
        bw.newLine();
        bw.close();
    }

}

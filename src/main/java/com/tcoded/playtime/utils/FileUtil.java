package com.tcoded.playtime.utils;

import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.UUID;
import java.util.stream.Collectors;

public class FileUtil {

    @Nullable
    public static String readFile(File file) throws FileNotFoundException {
        FileReader in = new FileReader(file);
        BufferedReader buf = new BufferedReader(in);
        return buf.lines().collect(Collectors.joining("\n"));
    }

    public static void overwriteFile(File file, String data) throws IOException {
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try (FileWriter writer = new FileWriter(file, false)) {
            BufferedWriter buf = new BufferedWriter(writer);
            buf.write(data);
            buf.flush();
        } // auto-close
    }
}

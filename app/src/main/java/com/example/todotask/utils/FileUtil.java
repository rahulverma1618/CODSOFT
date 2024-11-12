package com.example.todotask.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import android.util.Log;

public class FileUtil {

    // Method to write data to a file
    public static void writeFile(File file, String data) {
        try (FileOutputStream fos = new FileOutputStream(file);
             OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8)) {
             osw.write(data);
             osw.flush();
        } catch (IOException e) {
            Log.e("File Write Error", "Error writing data to file: " + file.getName(), e);
        }
    }

    public static String readFile(File file) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(file);

        byte[] buffer = new byte[(int) file.length()];
        fileInputStream.read(buffer);
        fileInputStream.close();
        String data = new String(buffer, StandardCharsets.UTF_8);

        return data;
    }
}

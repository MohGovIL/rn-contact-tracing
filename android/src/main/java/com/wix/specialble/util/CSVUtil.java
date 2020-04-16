package com.wix.specialble.util;

import android.content.Context;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.wix.specialble.bt.Device;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import de.siegmar.fastcsv.writer.CsvAppender;
import de.siegmar.fastcsv.writer.CsvWriter;

public class CSVUtil {
    private static final String TAG = CSVUtil.class.getSimpleName();
    private static final String FILE_NAME = "allDevices";
    private static final String FILE_EXTENSION = ".csv";

    private static File getCsvFile(Context context) {
        File[] externalStorageVolumes =
                ContextCompat.getExternalFilesDirs(context.getApplicationContext(), null);
        String filenameExternal = externalStorageVolumes[0].getAbsolutePath() + "/" + FILE_NAME + "_" + System.currentTimeMillis() + FILE_EXTENSION;
        return new File(filenameExternal);
    }

    public static void saveAllDevicesAsCSV(final Context context, List<Device> allDevices) {
        CsvWriter csvWriter = new CsvWriter();
        try {
            CsvAppender csvAppender = csvWriter.append(getCsvFile(context), StandardCharsets.UTF_8);
            csvAppender.appendLine("publicKey", "deviceName", "deviceData", "deviceAddress", "rssi");
            for (Device device : allDevices) {
                csvAppender.appendField(device.getPublicKey());
                csvAppender.appendField(device.getDeviceName());
                csvAppender.appendField(device.getDeviceData());
                csvAppender.appendField(device.getDeviceAddress());
                csvAppender.appendField(String.valueOf(device.getRssi()));
                csvAppender.endLine();
            }
            csvAppender.close();
        } catch (Exception e) {
            Log.e(TAG, "saveAllDevicesJsonAsCSV: " + e.getMessage(), e);
        }
    }

    private static void readCsvFile(Context context) {
        //Get the text file
        File readfile = getCsvFile(context);
        //Read text from file
        StringBuilder text = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new FileReader(readfile));
            String line;
            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
            br.close();
        } catch (IOException e) {
            //You'll need to add proper error handling here
            Log.e(TAG, "readCsvFile | "+e.getMessage());
        }
    }
}

package com.wix.specialble.util;

import android.content.Context;

import androidx.core.content.ContextCompat;

import com.wix.specialble.bt.Device;
import com.wix.specialble.bt.Scan;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;

import de.siegmar.fastcsv.writer.CsvAppender;
import de.siegmar.fastcsv.writer.CsvWriter;

public class CSVUtil {
    private static final String TAG = CSVUtil.class.getSimpleName();
    private static final String FILE_NAME_DEVICES = "allDevices";
    private static final String FILE_NAME_SCANS = "allScans";
    private static final String FILE_EXTENSION = ".csv";

    public static File getDevicesCsvFile(Context context) {
        return getCsvFile(context, FILE_NAME_DEVICES);
    }

    public static File getScansCsvFile(Context context) {
        return getCsvFile(context, FILE_NAME_SCANS);
    }

    private static File getCsvFile(Context context, String filename) {
        File[] externalStorageVolumes =
                ContextCompat.getExternalFilesDirs(context.getApplicationContext(), null);
        String filenameExternal = externalStorageVolumes[0].getAbsolutePath() + "/" + filename + FILE_EXTENSION;
        return new File(filenameExternal);
    }

    public static void saveAllScansAsCSV(final Context context, List<Scan> allScans) throws Exception {
        CsvWriter csvWriter = new CsvWriter();
        CsvAppender csvAppender = csvWriter.append(getScansCsvFile(context), StandardCharsets.UTF_8);
        csvAppender.appendLine("timestamp", "publicKey", "deviceAddress", "deviceProtocol",
                "rssi", "proximity", "acceleration_x", "acceleration_y", "acceleration_z",
                "rotation_x", "rotation_y", "rotation_z", "rotation_scalar", "battery");
        for (Scan scan : allScans) {
            csvAppender.appendField(String.valueOf(scan.getTimestamp()));
            csvAppender.appendField(scan.getPublicKey());
            csvAppender.appendField(scan.getScanAddress());
            csvAppender.appendField(scan.getScanProtocol());
            csvAppender.appendField(String.valueOf(scan.getRssi()));
            csvAppender.appendField(String.valueOf(scan.getProximityValue()));
            csvAppender.appendField(String.valueOf(scan.getAccelerationX()));
            csvAppender.appendField(String.valueOf(scan.getAccelerationY()));
            csvAppender.appendField(String.valueOf(scan.getAccelerationZ()));
            csvAppender.appendField(String.valueOf(scan.getRotationVectorX()));
            csvAppender.appendField(String.valueOf(scan.getRotationVectorY()));
            csvAppender.appendField(String.valueOf(scan.getRotationVectorZ()));
            csvAppender.appendField(String.valueOf(scan.getRotationVectorScalar()));
            csvAppender.appendField(String.valueOf(scan.getBatteryLevel()));
            csvAppender.endLine();
        }
        csvAppender.close();
    }

    public static void saveAllDevicesAsCSV(final Context context, List<Device> allDevices) throws Exception {
        CsvWriter csvWriter = new CsvWriter();
        CsvAppender csvAppender = csvWriter.append(getDevicesCsvFile(context), StandardCharsets.UTF_8);
        csvAppender.appendLine("firstSeen", "lastSeen", "publicKey", "deviceAddress", "deviceProtocol", "rssi");
        for (Device device : allDevices) {
            csvAppender.appendField(String.valueOf(device.getFirstTimestamp()));
            csvAppender.appendField(String.valueOf(device.getLastTimestamp()));
            csvAppender.appendField(device.getPublicKey());
            csvAppender.appendField(device.getDeviceAddress());
            csvAppender.appendField(device.getDeviceProtocol());
            csvAppender.appendField(String.valueOf(device.getRssi()));
            csvAppender.endLine();
        }
        csvAppender.close();
    }
}
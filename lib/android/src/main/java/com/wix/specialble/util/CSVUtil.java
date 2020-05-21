package com.wix.specialble.util;

import android.content.Context;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.facebook.common.util.Hex;
import com.wix.crypto.Contact;
import com.wix.specialble.bt.Device;
import com.wix.specialble.bt.Scan;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

import de.siegmar.fastcsv.writer.CsvAppender;
import de.siegmar.fastcsv.writer.CsvWriter;

public class CSVUtil {
    private static final String TAG = CSVUtil.class.getSimpleName();
    private static final String FILE_NAME_DEVICES = "allDevices";
    private static final String FILE_NAME_SCANS = "allScans";
    private static final String FILE_EXTENSION = ".csv";
    private static final String FILE_NAME_CONTACTS = "allContacts";

    public static File getDevicesCsvFile(Context context) {
        return getCsvFile(context, FILE_NAME_DEVICES);
    }

    public static File getScansCsvFile(Context context) {
        return getCsvFile(context, FILE_NAME_SCANS);
    }

    public static File getScanByKeyCsvFile(Context context, String key ) {
        return getCsvFile(context, key + "_scans");
    }

    public static File getContactsCsvFile(Context context) {
        return  getCsvFile(context, FILE_NAME_CONTACTS);
    }

    private static File getCsvFile(Context context, String filename) {
        File[] externalStorageVolumes =
                ContextCompat.getExternalFilesDirs(context.getApplicationContext(), null);
        String filenameExternal = externalStorageVolumes[0].getAbsolutePath() + "/" + filename + FILE_EXTENSION;
        return new File(filenameExternal);
    }

    public static void saveAllScansAsCSV(final Context context, List<Scan> allScans) throws Exception {

        FileOutputStream fos = new FileOutputStream(getScansCsvFile(context));

        // write header line
        appendHeaderLine(fos, "timestamp", "publicKey", "deviceAddress", "deviceProtocol",
                "rssi", "tx", "proximity", "acceleration_x", "acceleration_y", "acceleration_z",
                "rotation_x", "rotation_y", "rotation_z", "rotation_scalar", "battery");

        for (Scan scan : allScans)
        {
            writeScan(fos, scan);
        }
        fos.close();
    }

    private static void appendHeaderLine(OutputStream dos, String... headers) throws IOException
    {
        StringBuilder sb = new StringBuilder();
        for (String header: headers)
        {
            sb.append(","); //we'll remove the 1st instance after the loop
            sb.append(header);
        }

        dos.write(sb.substring(1).getBytes()); // write header line, remove extra initial ','
        dos.write(System.lineSeparator().getBytes());
    }

    private static void writeScan(OutputStream dos, Scan scan) throws IOException
    {
        appendColumn(String.valueOf(scan.getTimestamp()), dos, false);
        appendColumn(scan.getPublicKey(), dos, true);
        appendColumn(scan.getScanAddress(), dos, true);
        appendColumn(scan.getScanProtocol(), dos, true);
        appendColumn(String.valueOf(scan.getRssi()), dos, true);
        appendColumn(String.valueOf(scan.getTx()), dos, true);
        appendColumn(String.valueOf(scan.getProximityValue()), dos, true);
        appendColumn(String.valueOf(scan.getAccelerationX()), dos, true);
        appendColumn(String.valueOf(scan.getAccelerationY()), dos, true);
        appendColumn(String.valueOf(scan.getAccelerationZ()), dos, true);
        appendColumn(String.valueOf(scan.getRotationVectorX()), dos, true);
        appendColumn(String.valueOf(scan.getRotationVectorY()), dos, true);
        appendColumn(String.valueOf(scan.getRotationVectorZ()), dos, true);
        appendColumn(String.valueOf(scan.getRotationVectorScalar()), dos, true);
        appendColumn(String.valueOf(scan.getBatteryLevel()), dos, true);
        dos.write(System.lineSeparator().getBytes());
    }

    private static void appendColumn(String column, OutputStream dos, boolean delimit) throws IOException
    {
        // construct csv column value
        if(column.matches("\\d+(?:\\.\\d+)?"))
        {
            // this is a number so we don't wrap
        }
        else
        {
            // this is text hence wrap
            column = "\"" + column.replaceAll("\"", "\"\"") + "\"";
        }


        dos.write(column.getBytes());
        if (delimit)
        {
            dos.write(",".getBytes());
        }

    }

    public static void saveScansByKeyAsCsv(final Context context, List<Scan> scans, String key) throws Exception {
        FileOutputStream fos = new FileOutputStream(getScanByKeyCsvFile(context, key));

        // write header line
        appendHeaderLine(fos, "timestamp", "publicKey", "deviceAddress", "deviceProtocol",
                "rssi", "tx", "proximity", "acceleration_x", "acceleration_y", "acceleration_z",
                "rotation_x", "rotation_y", "rotation_z", "rotation_scalar", "battery");

        for (Scan scan : scans)
        {
            writeScan(fos, scan);
        }
        fos.close();
    }

    public static void saveAllDevicesAsCSV(final Context context, List<Device> allDevices) throws Exception {
        FileOutputStream fos = new FileOutputStream(getDevicesCsvFile(context));

        // write header line
        appendHeaderLine(fos, "firstSeen", "lastSeen", "publicKey", "deviceAddress", "deviceProtocol", "rssi");

        for (Device device : allDevices) {
            writeDevice(device, fos);
        }

        fos.close();
    }

    private static void writeDevice(Device device, OutputStream dos) throws IOException
    {
        appendColumn(String.valueOf(device.getFirstTimestamp()), dos, false);
        appendColumn(String.valueOf(device.getLastTimestamp()), dos, true);
        appendColumn(device.getPublicKey(), dos, true);
        appendColumn(device.getDeviceAddress(), dos, true);
        appendColumn(device.getDeviceProtocol(), dos, true);
        appendColumn(String.valueOf(device.getRssi()), dos, true);
        dos.write(System.lineSeparator().getBytes());
    }

    public static void saveAllContactsAsCSV(final Context context, List<Contact> contacts) throws Exception {
        FileOutputStream fos = new FileOutputStream(getContactsCsvFile(context));

        appendHeaderLine(fos,"ephemeral_id", "rssi", "timestamp", "geohash");

        for(Contact contact : contacts) {
            writeContact(contact, fos);
        }
        fos.close();
    }

    private static void writeContact(Contact contact, OutputStream dos) throws IOException
    {
        appendColumn(Hex.encodeHex(contact.getEphemeral_id(), false), dos, true);
        appendColumn(Hex.encodeHex(contact.getRssi(), false), dos, true);
        appendColumn(String.valueOf(contact.getTimestamp()), dos, true);
        appendColumn(Hex.encodeHex(contact.getGeohash(), false), dos, true);
        dos.write(System.lineSeparator().getBytes());
    }
}
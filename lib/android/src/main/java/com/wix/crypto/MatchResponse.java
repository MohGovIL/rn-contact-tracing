package com.wix.crypto;

import com.wix.crypto.utilities.Hex;

import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

public class MatchResponse {

    private long startContactTimestamp;
    private long endContactTimestamp;

    private List<String> verifiedEphemerals;
    private double lat;
    private double lon;
    private String contactIntegrityLevel;

    public MatchResponse(long startContactTimestamp, long endContactTimestamp, List<String> verifiedEphemerals, double lat, double lon, String contactIntegrityLevel) {
        this.startContactTimestamp = startContactTimestamp;
        this.endContactTimestamp = endContactTimestamp;
        this.verifiedEphemerals = verifiedEphemerals;
        this.lat = lat;
        this.lon = lon;
        this.contactIntegrityLevel = contactIntegrityLevel;
    }

    public long getStartContactTimestamp() {
        return startContactTimestamp;
    }

    public void setStartContactTimestamp(long startContactTimestamp) {
        this.startContactTimestamp = startContactTimestamp;
    }

    public long getEndContactTimestamp() {
        return endContactTimestamp;
    }

    public void setEndContactTimestamp(long endContactTimestamp) {
        this.endContactTimestamp = endContactTimestamp;
    }

    public List<String> getVerifiedEphemerals() {
        return verifiedEphemerals;
    }

    public void setVerifiedEphemerals(List<String> verifiedEphemerals) {
        this.verifiedEphemerals = verifiedEphemerals;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public String getContactIntegrityLevel() {
        return contactIntegrityLevel;
    }

    public void setContactIntegrityLevel(String contactIntegrityLevel) {
        this.contactIntegrityLevel = contactIntegrityLevel;
    }

    public JSONObject toJsonObject() {

        JSONObject jo = new JSONObject();

        try
        {
            jo.put("startContactTimestamp", String.valueOf(startContactTimestamp));
            jo.put("endContactTimestamp", String.valueOf(endContactTimestamp));
            jo.put("verifiedEphemerals", verifiedEphemerals);
            jo.put("lat",lat);
            jo.put("lon",lon);
            jo.put("contactIntegrityLevel",contactIntegrityLevel);

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return jo;
    }
}

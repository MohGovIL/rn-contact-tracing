package moh.gov.il.crypto;

import androidx.annotation.Nullable;
import com.google.gson.annotations.SerializedName;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.Objects;

/**
 * Created by hagai on 11/05/2020.
 */
public class Time implements Comparable<Time> {

    public static final int DAY = 24 * 60 * 60; // Day length in seconds
    public static final int EPOCH = 60 * 60; // 1 hour - Te
    public static final int UNIT = 5 * 60; // 5 minues - Tu
    public static final int UNITS_IN_EPOCH = EPOCH / UNIT;
    public static final int JITTER_THRESHOLD = 10 * 60;
    public static final int EPOCHS_IN_DAY = DAY / EPOCH;
    public static final int WINDOW = 5 * 60;
    public static final int MAX_CONTACTS_IN_WINDOW = 1000;

    @SerializedName("Time")
    private int mTime;
    @SerializedName("Day")
    private int mDay;
    @SerializedName("Epoch")
    private int mEpoch;

    //TODO: create two constructors one with two params and one with only unix time
    public Time(int unixTime, int epoch) {

        if(epoch == Constants.None) {

            this.mTime = unixTime;
            this.mDay = unixTime / DAY;
            this.mEpoch = (unixTime % DAY) / EPOCH;

        } else {

            assert epoch < EPOCHS_IN_DAY : "Epoch " + epoch + "must be < " + EPOCHS_IN_DAY;
            this.mDay = unixTime;
            this.mEpoch = (int)epoch;
        }
    }

    private Time()
    {

    }

    public JSONObject toJson() throws JSONException
    {
        JSONObject jo = new JSONObject();
        jo.put("Time", mTime);
        jo.put("Day", mDay);
        jo.put("Epoch", mEpoch);
        return jo;
    }


    public static Time fromJson(JSONObject jo) throws JSONException
    {
        Time t = new Time();
        t.mTime = jo.getInt("Time");
        t.mDay = jo.getInt("Day");
        t.mEpoch = jo.getInt("Epoch");
        return t;
    }


    public int getUnits()
    {
        return (this.mTime - this.mDay*DAY - this.mEpoch*EPOCH) / UNIT;
    }

    public Time getNext()
    {
        if (this.mEpoch == EPOCHS_IN_DAY-1) {
            return new Time(this.mDay + 1, 0);
        }
        return new Time(this.mDay, this.mEpoch+1);
    }

    public String getStrWithUnits()
    {
        return this.mDay + " " + this.mEpoch + " " + getUnits();
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(mDay, mEpoch);
    }

    @Override
    public int compareTo(Time other)
    {
        if(this.mDay == other.mDay && this.mEpoch == other.mEpoch)
            return 0;
        else if(EPOCHS_IN_DAY*this.mDay + this.mEpoch < EPOCHS_IN_DAY*other.mDay + other.mEpoch)
            return -1;
        else
            return 1;
    }

    @Override
    public boolean equals(@Nullable Object obj)
    {
        if (obj == null)
        {
            return false;
        }

        if (!Time.class.isAssignableFrom(obj.getClass())) {
            return false;
        }

        final Time other = (Time) obj;

        return this.mDay == other.mDay && this.mEpoch == other.mEpoch;
    }

    public static int dayToSeconds(int day) { return DAY * day; }

    public int getTime() { return mTime; }

    public int getDay() { return mDay; }

    public int getEpoch() { return mEpoch; }

    public boolean largerEqualThan(Time start)
    {
        return EPOCHS_IN_DAY*this.mDay + this.mEpoch >= EPOCHS_IN_DAY*start.mDay + start.mEpoch;
    }
}

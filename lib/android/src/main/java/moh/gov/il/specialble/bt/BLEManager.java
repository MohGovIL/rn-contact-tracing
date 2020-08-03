package moh.gov.il.specialble.bt;


import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.util.Log;

import moh.gov.il.specialble.db.DBClient;
import moh.gov.il.crypto.Contact;
import moh.gov.il.specialble.EventToJSDispatcher;
import moh.gov.il.specialble.config.Config;
import moh.gov.il.specialble.listeners.IEventListener;
import moh.gov.il.specialble.util.Constants;

import java.util.List;


public class BLEManager implements IEventListener {

    private static final String TAG = "BLEManager";

    private BluetoothAdapter bluetoothAdapter;
    Context context;
    private BLEScannerManager bleScanner;
    private BLEAdvertisingManager bleAdvertiser;
    private static BLEManager sBLEManagerInstance;
    private Config mConfig;
    private EventToJSDispatcher mEventToJSDispatcher;

    public enum BLEProtocol {
        GAP, GATT
    }

    private BLEManager(Context context) {
        this.context = context;
        init();
    }

    public static BLEManager getInstance(Context context) {
        if (sBLEManagerInstance == null) {
            sBLEManagerInstance = new BLEManager(context);
        }
        return sBLEManagerInstance;
    }

    public static BLEManager getInstance() throws Exception {
        if (sBLEManagerInstance == null) {
            throw new Exception("missing context");
        }
        return sBLEManagerInstance;
    }

    public void init() {
        mConfig = Config.getInstance(context);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter != null) {
            bleScanner = new BLEScannerManager(context, this);
            bleAdvertiser = new BLEAdvertisingManager(context, this);
        }
    }

    public void startScan() {
        bleScanner.startScan(mConfig.getServiceUUID());
    }

    public void stopScan() {
        bleScanner.stopScan();
    }

    public void advertise()
    {
        bleAdvertiser.startAdvertise(mConfig.getServiceUUID());
    }

    public void stopAdvertise() {
        bleAdvertiser.stopAdvertise();
    }


    public List<Device> getAllDevices() {
        return DBClient.getInstance(context).getAllDevices();
    }

    public void clearAllDevices() {
        DBClient.getInstance(context).clearAllDevices();
    }

    public List<Scan> getAllScans() {
        return DBClient.getInstance(context).getAllScans();
    }

    public List<Scan> getScansByKey(String pubKey) {
        return DBClient.getInstance(context).getScansByKey(pubKey);
    }

    public List<Contact> getAllContacts() { return DBClient.getInstance(context).getAllContacts(); }

    public void wipeDatabase() {
        DBClient.getInstance(context).deleteDatabase();
    }

    public void setEventToJSDispatcher(EventToJSDispatcher eventToJSDispatcher) {
        mEventToJSDispatcher = eventToJSDispatcher;
    }

    public List<Event> getAllAdvertiseData() {
        return DBClient.getInstance(context).getEventsByActionType(Constants.ACTION_ADVERTISE);
    }

    public List<Event> getAllScansData() {
        return DBClient.getInstance(context).getEventsByActionType(Constants.ACTION_SCAN);
    }

    @Override
    public void onEvent(String event, Object data) {
        if (mEventToJSDispatcher != null) {
            mEventToJSDispatcher.onEvent(event, data);
        } else {
            Log.d(TAG, "BLEManager onEvent() | cannot send to JSDispatcher - it's null | event = " + event + ", data = " + data);
            // can be null if application flow started from BroadcastReceiver (boot/restart/upgrade).
            // Service information is still saved to db, but ReactNative UI will not be updated.
        }
    }
}
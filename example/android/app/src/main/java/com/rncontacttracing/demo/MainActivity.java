package com.rncontacttracing.demo;

import android.content.Intent;

import com.facebook.react.ReactActivity;
import com.wix.specialble.bt.BLEManager;
import com.wix.specialble.util.DeviceUtil;

public class MainActivity extends ReactActivity {

  /**
   * Returns the name of the main component registered from JavaScript. This is used to schedule
   * rendering of the component.
   */
  @Override
  protected String getMainComponentName() {
    return "example";
  }


  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    if(requestCode== DeviceUtil.IGNORE_BATTERY_OPTIMIZATIONS_REQUEST_CODE){
      try {
        BLEManager bleManager = BLEManager.getInstance();
        bleManager.onEvent("checkPermission", null);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    super.onActivityResult(requestCode, resultCode, data);
  }
}

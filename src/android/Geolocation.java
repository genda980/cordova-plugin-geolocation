package org.apache.cordova.geolocation;

import android.content.Intent;
import android.os.Build;
import android.text.TextUtils;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This class echoes a string called from JavaScript.
 */
public class Geolocation extends CordovaPlugin {

    private static final String TAG = "Geolocation";

    private static CordovaWebView sCordovaWebView;
    static ServiceObj sServiceObj;

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        JSONObject jsonObject = new JSONObject(args.optString(0, ""));
        if (action.equals("getPosition")) {
            getPosition(callbackContext);
            return true;
        }
        if (action.equals("startLocation")) {
            startLocation(jsonObject, callbackContext);
            return true;
        }
        if (action.equals("stopLocation")) {
            stopLocation(callbackContext);
            return true;
        }
        if (action.equals("parseLocation")) {
            parseLocation(jsonObject, callbackContext);
            return true;
        }
        if (action.equals("openMapApp")) {
            openMapApp(jsonObject, callbackContext);
            return true;
        }
        return false;
    }

    private void getPosition(CallbackContext callbackContext) {
        LocationManger.getInstance().getDefOnceLocation(cordova.getContext(), new LocationManger.IGeolocationListener() {
            @Override
            public void onSuccess(LocationObj locationObj) {
                JSONObject jsonObject = LocationUtils.parseBackObj(locationObj);
                callbackContext.success(jsonObject);
            }

            @Override
            public void onError(int errorCode, String msg) {
                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("code", errorCode);
                    jsonObject.put("msg", msg);
                    callbackContext.error(jsonObject);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void startLocation(JSONObject obj, CallbackContext callbackContext) {
        sCordovaWebView = webView;
        cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    int intervalTime = obj.optInt("intervalTime", 5);
                    int diffDistance = obj.optInt("diffDistance", 500);
                    String notifyTitle = obj.optString("notifyTitle", "消息服务");
                    sServiceObj = new ServiceObj(intervalTime, diffDistance, notifyTitle);

                    Intent intent = new Intent(cordova.getContext(), LocationService.class);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        cordova.getContext().startForegroundService(intent);
                    } else {
                        cordova.getContext().startService(intent);
                    }
                    callbackContext.success();
                    return;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                callbackContext.error("Start Error");
            }
        });
    }

    private void stopLocation(CallbackContext callbackContext) {
        try {
            cordova.getContext().stopService(new Intent(cordova.getContext(), LocationService.class));
            callbackContext.success();
            return;
        } catch (Exception e) {
            e.printStackTrace();
        }
        callbackContext.error("Stop error");
    }

    private void parseLocation(JSONObject obj, CallbackContext callbackContext) {
        String longitude = obj.optString("longitude", "");
        String latitude = obj.optString("latitude", "");
        if (TextUtils.isEmpty(longitude) || TextUtils.isEmpty(latitude)) {
            callbackContext.error("params error");
        }
        LocationObj locationObj = LocationUtils.parseLocation(cordova.getContext(), Double.parseDouble(latitude), Double.parseDouble(longitude));
        if (locationObj == null) {
            callbackContext.error("Parse Error");
            return;
        }
        callbackContext.success(LocationUtils.parseBackObj(locationObj));
    }

    private void openMapApp(JSONObject obj, CallbackContext callbackContext) {
        int type = obj.optInt("type", 0);
        String longitude = obj.optString("longitude", "");
        String latitude = obj.optString("latitude", "");
        String endName = obj.optString("endName", "");
        if (type <= 0 || type >= 4) {
            callbackContext.error("Params type error");
            return;
        }
        if (TextUtils.isEmpty(longitude) || TextUtils.isEmpty(latitude)) {
            callbackContext.error("Params lng_lat error");
            return;
        }

        boolean openStatus;
        if (type == 1) {
            openStatus = LocationUtils.openMapGaode(cordova.getContext(), latitude, longitude, endName);
        } else if (type == 2) {
            openStatus = LocationUtils.openMapBaidu(cordova.getContext(), latitude, longitude, endName);
        } else {
            openStatus = LocationUtils.openMapTengxun(cordova.getContext(), latitude, longitude, endName);
        }
        if (openStatus) {
            callbackContext.error("Open error");
        } else {
            callbackContext.success();
        }
    }

    public static CordovaWebView getLocationWeb() {
        return sCordovaWebView;
    }

    static class ServiceObj {
        public int intervalTime;
        public int diffDistance;
        public String notifyTitle;

        public ServiceObj(int intervalTime, int diffDistance, String notifyTitle) {
            this.intervalTime = intervalTime;
            this.diffDistance = diffDistance;
            this.notifyTitle = notifyTitle;
        }
    }
}

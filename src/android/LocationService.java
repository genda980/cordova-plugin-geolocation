package org.apache.cordova.geolocation;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;

import org.json.JSONObject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class LocationService extends Service implements AMapLocationListener {

    private static final String TAG = "LocationService";

    private AMapLocationClient mLocationClient;

    public LocationService() {
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        createNotification(this);

        if (mLocationClient != null) return;

        mLocationClient = new AMapLocationClient(this);
        AMapLocationClientOption option = new AMapLocationClientOption();
        option.setOnceLocation(false);
        option.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        option.setInterval(Geolocation.sServiceObj.intervalTime * 60 * 1000);
        mLocationClient.setLocationOption(option);
        mLocationClient.setLocationListener(this);
        mLocationClient.startLocation();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        stopForeground(true);
        if (mLocationClient != null) {
            mLocationClient.unRegisterLocationListener(this);
            mLocationClient.stopLocation();
            mLocationClient.onDestroy();
            mLocationClient = null;
        }
    }

    @Override
    public void onLocationChanged(final AMapLocation aMapLocation) {
        Log.e(TAG, "onLocationChanged: '----> " + aMapLocation);
        if (aMapLocation == null) return;
        if (aMapLocation.getErrorCode() != AMapLocation.LOCATION_SUCCESS) return;
        try {
            handleLocation(aMapLocation);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createNotification(@NonNull Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            NotificationChannel channel = new NotificationChannel(context.getPackageName(), context.getPackageName(), NotificationManager.IMPORTANCE_HIGH);
            channel.enableVibration(false);
            channel.enableLights(false);
            channel.enableVibration(false);
            channel.setVibrationPattern(new long[]{0});
            channel.setSound(null, null);
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            Notification notification = new Notification.Builder(context, context.getPackageName())
                    .setContentTitle(Geolocation.sServiceObj.notifyTitle)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent).build();
            startForeground(11596, notification);
        }
    }

    private void handleLocation(AMapLocation aMapLocation) {
        String lastLong = LocationUtils.getValue(this, "last_long", "0");
        String lastLat = LocationUtils.getValue(this, "last_lat", "0");

        boolean needBack;
        if (TextUtils.equals(lastLong, "0") || TextUtils.equals(lastLat, "0")) {
            needBack = true;
        } else {
            float lineDistanceM = LocationUtils.getLineDistanceM(String.valueOf(aMapLocation.getLatitude()), String.valueOf(aMapLocation.getLongitude()), lastLat, lastLong);
            needBack = lineDistanceM > Geolocation.sServiceObj.diffDistance;
        }

        LocationUtils.setValue(this, "last_long", String.valueOf(aMapLocation.getLongitude()));
        LocationUtils.setValue(this, "last_lat", String.valueOf(aMapLocation.getLatitude()));

        if (!needBack) return;
        LocationObj locationObj = LocationUtils.getLocationObj(aMapLocation);
        JSONObject resJson = LocationUtils.parseBackObj(locationObj);
        @SuppressLint("DefaultLocale") final String jsStr =
                String.format("Geolocation.onLocationEvent(%d, %s)", 1, resJson.toString());
        Geolocation.getLocationWeb().loadUrl("javascript:" + jsStr);
    }
}

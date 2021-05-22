package org.apache.cordova.geolocation;

import android.content.Context;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;

class LocationManger {

    private static final String TAG = "LocationManger";

    private LocationManger() {

    }

    public static LocationManger getInstance() {
        return LocationMangerHolder.sInstance;
    }

    private static class LocationMangerHolder {
        private static final LocationManger sInstance = new LocationManger();
    }

    public void getDefOnceLocation(Context context, IGeolocationListener iGeolocationListener) {
        AMapLocationClient locationClient = new AMapLocationClient(context);
        AMapLocationClientOption locationClientOption = getLocationOption();
        locationClient.setLocationOption(locationClientOption);
        locationClient.setLocationListener(new AMapLocationListener() {
            @Override
            public void onLocationChanged(AMapLocation aMapLocation) {
                if (aMapLocation == null) {
                    iGeolocationListener.onError(-1, "Get Error");
                    return;
                }
                if (aMapLocation.getErrorCode() == AMapLocation.LOCATION_SUCCESS) {
                    LocationObj locationObj = LocationUtils.getLocationObj(aMapLocation);
                    if (locationObj == null) {
                        iGeolocationListener.onError(-1, "Get Error LocationObj");
                    } else {
                        iGeolocationListener.onSuccess(locationObj);
                    }
                } else {
                    iGeolocationListener.onError(aMapLocation.getErrorCode(), aMapLocation.getErrorInfo());
                }
            }
        });
        locationClient.startLocation();
    }

    /**
     * 定位参数
     */
    private AMapLocationClientOption getLocationOption() {
        AMapLocationClientOption option = new AMapLocationClientOption();
        // 可选，设置定位模式，可选的模式有高精度、仅设备、仅网络。
        option.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        // 可选，设置是否gps优先，只在高精度模式下有效。默认关闭
        option.setGpsFirst(false);
        // 可选，设置网络请求超时时间。在仅设备模式下无效
        option.setHttpTimeOut(30000);
        // 可选，设置定位间隔
        option.setInterval(2000);
        // 可选，设置是否返回逆地理地址信息。
        option.setNeedAddress(true);
        // 可选，设置是否单次定位
        option.setOnceLocation(true);
        // 可选， 设置网络请求的协议。可选HTTP或者HTTPS。
        AMapLocationClientOption.setLocationProtocol(AMapLocationClientOption.AMapLocationProtocol.HTTP);
        // 可选，设置是否使用传感器。
        option.setSensorEnable(false);
        // 可选，设置是否开启wifi扫描。
        // 如果设置为false会同时停止主动刷新，停止以后完全依赖于系统刷新，定位位置可能存在误差
        option.setWifiScan(true);
        // 可选，设置是否使用缓存定位.
        option.setLocationCacheEnable(false);
        return option;
    }

    public interface IGeolocationListener {

        void onSuccess(LocationObj locationObj);

        void onError(int errorCode, String msg);

    }
}
package org.apache.cordova.geolocation;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;

import org.json.JSONObject;

import java.util.List;
import java.util.Locale;

class LocationUtils {

    public static LocationObj parseLocation(Context context, double lat, double lng) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        List<Address> locationList;
        try {
            locationList = geocoder.getFromLocation(lat, lng, 1);
            if (locationList != null) {
                Address address = locationList.get(0);
                String countryCode = address.getCountryCode();
                String country = address.getCountryName();
                String prov = address.getAdminArea();
                String city = address.getLocality();
                String area = address.getSubLocality();
                String street = address.getThoroughfare();
                String addressLine = "";
                String addressLineTwo = "";

                if (address.getMaxAddressLineIndex() >= 1) {
                    addressLine = address.getAddressLine(0);
                    addressLineTwo = address.getAddressLine(1);
                } else {
                    addressLine = address.getAddressLine(0);
                }
                for (int i = 0; address.getAddressLine(i) != null; i++) {
                    System.out.println("addressLine=====" + address.getAddressLine(i));
                }
                LocationObj locationObj = new LocationObj(lng, lat);
                locationObj.countryCode = countryCode;
                locationObj.country = country;
                locationObj.prov = prov;
                locationObj.city = city;
                locationObj.area = area;
                locationObj.street = street;
                locationObj.address = addressLine;
                locationObj.addressTwo = addressLineTwo;
                return locationObj;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static LocationObj getLocationObj(AMapLocation location) {
        if (location == null) return null;
        LocationObj locationObj = new LocationObj(location.getLongitude(), location.getLatitude());
        locationObj.countryCode = "";
        locationObj.country = location.getCountry();
        locationObj.prov = location.getProvince();
        locationObj.city = location.getCity();
        locationObj.area = location.getDistrict();
        locationObj.street = location.getStreet();
        locationObj.address = location.getAddress();
        locationObj.addressTwo = "";
        return locationObj;
    }

    public static JSONObject parseBackObj(LocationObj obj) {
        JSONObject result = new JSONObject();
        if (obj == null) return result;
        try {
            result.put("longitude", obj.longitude);
            result.put("latitude", obj.latitude);
            result.put("countryCode", obj.countryCode);
            result.put("country", obj.country);
            result.put("prov", obj.prov);
            result.put("city", obj.city);
            result.put("area", obj.area);
            result.put("street", obj.street);
            result.put("address", obj.address);
            result.put("addressTwo", obj.addressTwo);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static boolean openMapGaode(Context context, String latitude, String longitude, String dName) {
        try {
            if (!checkMapAppsIsExist(context, "com.autonavi.minimap")) {
                Toast.makeText(context, "未安装高德地图", Toast.LENGTH_SHORT).show();
                return false;
            } else {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setPackage("com.autonavi.minimap");
                intent.addCategory("android.intent.category.DEFAULT");
                intent.setData(Uri.parse("androidamap://route?sourceApplication=amap"
                        + "&sname=我的位置&dlat=" + latitude
                        + "&dlon=" + longitude
                        + "&dname=" + dName
                        + "&dev=0&m=0&t=1"));
                context.startActivity(intent);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean openMapBaidu(Context context, String latitude, String longitude, String dName) {
        try {
            if (!checkMapAppsIsExist(context, "com.baidu.BaiduMap")) {
                Toast.makeText(context, "未安装百度地图", Toast.LENGTH_SHORT).show();
                return false;
            } else {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("baidumap://map/direction?destination=name:" + dName + "|latlng:" + latitude + "," + longitude
                        + "&coord_type= bd09ll"));
                context.startActivity(intent);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean openMapTengxun(Context context, String latitude, String longitude, String dName) {
        try {
            if (!checkMapAppsIsExist(context, "com.tencent.map")) {
                Toast.makeText(context, "未安装腾讯地图", Toast.LENGTH_SHORT).show();
                return false;
            } else {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("qqmap://map/routeplan?type=bus&from=我的位置&fromcoord=0,0"
                        + "&to=" + dName
                        + "&tocoord=" + latitude + "," + longitude
                        + "&policy=1&referer=myapp"));
                context.startActivity(intent);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private static boolean checkMapAppsIsExist(Context context, String packageName) {
        PackageInfo packageInfo;
        try {
            packageInfo = context.getPackageManager().getPackageInfo(packageName, 0);
        } catch (Exception e) {
            packageInfo = null;
            e.printStackTrace();
        }
        return packageInfo != null;
    }

    public static final double EARTH_RADIUS = 6378137.0;
    public static float getLineDistanceM(String lat1, String lng1, String lat2, String lng2) {
        double lat_a = Double.parseDouble(lat1);
        double lng_a = Double.parseDouble(lng1);
        double lat_b = Double.parseDouble(lat2);
        double lng_b = Double.parseDouble(lng2);

        double radLat1 = (lat_a * Math.PI / 180.0);
        double radLat2 = (lat_b * Math.PI / 180.0);
        double a = radLat1 - radLat2;
        double b = (lng_a - lng_b) * Math.PI / 180.0;
        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2)
                + Math.cos(radLat1) * Math.cos(radLat2)
                * Math.pow(Math.sin(b / 2), 2)));
        s = s * EARTH_RADIUS;
        s = Math.round(s * 10000) / 10000;
        return (float) s;
    }

    static void setValue(Context context, String key, String value) {
        SharedPreferences sp = context.getSharedPreferences("location", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(key, value);
        editor.apply();
    }

    static String getValue(Context context, String key, String defValue) {
        SharedPreferences sp = context.getSharedPreferences("location", Context.MODE_PRIVATE);
        return sp.getString(key, defValue);
    }
}

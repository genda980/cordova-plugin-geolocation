package org.apache.cordova.geolocation;

import java.math.BigDecimal;

class LocationObj {

    public String longitude;
    public String latitude;
    public String countryCode;
    public String country;
    public String prov;
    public String city;
    public String area;
    public String street;
    public String address;
    public String addressTwo;

    public LocationObj(double longitude, double latitude) {
        BigDecimal bdLongitude, bdLatitude;
        bdLongitude = new BigDecimal(longitude);
        bdLongitude = bdLongitude.setScale(6, BigDecimal.ROUND_HALF_UP);
        bdLatitude = new BigDecimal(latitude);
        bdLatitude = bdLatitude.setScale(6, BigDecimal.ROUND_HALF_UP);

        this.longitude = bdLongitude + "";
        this.latitude = bdLatitude + "";
    }
}
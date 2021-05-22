var exec = require('cordova/exec');

// 返回数据 -> 1
// longitude;       保留小数点后6位
// latitude;        保留小数点后6位
// countryCode;     国家code          可选/也可返回空字符串
// country;         国家
// prov;            省
// city;            城市
// area;            区
// street;          街道
// address;         详细地址
// addressTwo;      详细地址备用        可选/也可返回空字符串

//  获取定位信息 返回数据 -> 1
exports.getPosition = function (success, error) {
    exec(success, error, 'Geolocation', 'getPosition', [{}]);
};

// 开启后台持续定位
// args
    // intervalTime -> 获取位置的时间间隔 单位分钟， 默认 5
    // diffDistance -> 与上次获取的位置比较值，单位 米。 默认 500 和上次位置比较大于此数值才返回数据
// 通过 onLocationEvent 监听，返回 js
exports.startLocation = function (args, success, error) {
    exec(success, error, 'Geolocation', 'startLocation', [args]);
};

// 停止后台持续定位
exports.stopLocation = function (success, error) {
    exec(success, error, 'Geolocation', 'stopLocation', [{}]);
};

// 通过 经纬度 获取地理信息
// args
    // longitude
    // latitude
// 返回数据 -> 1
exports.parseLocation = function (args, success, error) {
    exec(success, error, 'Geolocation', 'parseLocation', [args]);
};

// 打开地图导航
// args
    // longitude        目的地经度
    // latitude         目的地纬度
    // endName          目的地地点名称
    // type             地图app类型  （见下）
exports.MAP_GAODE = 1;
exports.MAP_BAIDU = 2;
exports.MAP_TENGXUN = 3;
exports.openMapApp = function (args, success, error) {
    exec(success, error, 'Geolocation', 'openMapApp', [args]);
};

// 后台持续定位 监听
// eventId      值为 1
// params       值为 返回数据 -> 1 格式
exports.onLocationEvent = function (eventId, params) {
    cordova.fireDocumentEvent('Geolocation.onLocationEvent', {
        eventId: eventId,
        params: params,
    });
};

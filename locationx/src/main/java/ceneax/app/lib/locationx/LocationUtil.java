package ceneax.app.lib.locationx;

import android.location.Location;

import java.util.Objects;

public class LocationUtil {
    // 两分钟，毫秒值
    private static final int TWO_MINUTES = 1000 * 60 * 2;
    // 地球半径6378.137，单位为千米
    private static final double EARTH_RADIUS = 6378.137;
    // 圆周率PI
    private static final double PI = Math.PI;
    // 卫星椭球坐标投影到平面地图坐标系的投影因子
    private static final double AXIS = 6378245.0;
    // 椭球的偏心率(a^2 - b^2) / a^2
    private static final double OFFSET = 0.00669342162296594323;
    // 圆周率转换量
    private static final double X_PI = PI * 3000.0 / 180.0;

    public static boolean isBetterLocation(Location newLocation, Location currentBestLocation) {
        if (newLocation == null) {
            return false;
        }
        if (currentBestLocation == null) {
            return true;
        }

        long timeDelta = newLocation.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        if (isSignificantlyNewer) {
            return true;
        } else if (isSignificantlyOlder) {
            return false;
        }

        int accuracyDelta = (int) (newLocation.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;
        boolean isFromSameProvider = Objects.equals(newLocation.getProvider(), currentBestLocation.getProvider());

        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else {
            return isNewer && !isSignificantlyLessAccurate && isFromSameProvider;
        }
    }

    private static double rad(double d) {
        return d * PI / 180.0;
    }

    /**
     * 计算两坐标点间的距离
     * @param lat1 坐标1维度
     * @param lng1 坐标1经度
     * @param lat2 坐标2维度
     * @param lng2 坐标2经度
     * @return 单位 KM
     */
    public static double getDistance(double lat1, double lng1, double lat2, double lng2) {
        double radLat1 = rad(lat1);
        double radLat2 = rad(lat2);
        double a = radLat1 - radLat2;
        double b = rad(lng1) - rad(lng2);
        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2) + Math.cos(radLat1) * Math.cos(radLat2) * Math.pow(Math.sin(b / 2), 2)));
        s = s * EARTH_RADIUS;

        return s;
    }

    /**
     * 用于 {@link CoordType#GCJ02} 转换为 {@link CoordType#BD09}
     * @param lat gcj02维度
     * @param lng gcj02经度
     * @return 维经数组（bd09）
     */
    public static double[] gcj02ToBd09(double lat, double lng) {
        double[] latLng = new double[2];
        double z = Math.sqrt(lng * lng + lat * lat) + 0.00002 * Math.sin(lat * X_PI);
        double theta = Math.atan2(lat, lng) + 0.000003 * Math.cos(lng * X_PI);
        latLng[0] = z * Math.sin(theta) + 0.006;
        latLng[1] = z * Math.cos(theta) + 0.0065;
        return latLng;
    }

    /**
     * 用于 {@link CoordType#BD09} 转换为 {@link CoordType#GCJ02}
     * @param lat bd09维度
     * @param lng bd09经度
     * @return 维经数组（gcj02）
     */
    public static double[] bd09ToGcj02(double lat, double lng) {
        double x = lng - 0.0065;
        double y = lat - 0.006;
        double[] latLng = new double[2];
        double z = Math.sqrt(x * x + y * y) - 0.00002 * Math.sin(y * X_PI);
        double theta = Math.atan2(y, x) - 0.000003 * Math.cos(x * X_PI);
        latLng[0] = z * Math.sin(theta);
        latLng[1] = z * Math.cos(theta);
        return latLng;
    }

    /**
     * 用于 {@link CoordType#BD09} 转为 {@link CoordType#WGS84}
     * @param lat bd09维度
     * @param lng bd09经度
     * @return 维经数组（wgs84）
     */
    public static double[] bd09ToWgs84(double lat, double lng) {
        double[] latLng = bd09ToGcj02(lat, lng);
        return gcj02ToWgs84Fuzzy(latLng[0], latLng[1]);
    }

    /**
     * 用于 {@link CoordType#WGS84} 转为 {@link CoordType#BD09}
     * @param lat wgs84维度
     * @param lng wgs84经度
     * @return 维经数组（bd09）
     */
    public static double[] wgs84ToBd09(double lat, double lng) {
        double[] latLng = wgs84ToGcj02(lat, lng);
        return gcj02ToBd09(latLng[0], latLng[1]);
    }

    /**
     * 用于 {@link CoordType#WGS84} 转为 {@link CoordType#GCJ02}
     * @param lat wgs84维度
     * @param lng wgs84经度
     * @return 维经数组（gcj-02）
     */
    public static double[] wgs84ToGcj02(double lat, double lng) {
        double[] latLng = new double[2];
        if (outOfChina(lat, lng)) {
            latLng[0] = lat;
            latLng[1] = lng;
            return latLng;
        }
        double[] deltaD = transform(lat, lng);
        latLng[0] = lat + deltaD[0];
        latLng[1] = lng + deltaD[1];
        return latLng;
    }

    /**
     * 用于 {@link CoordType#GCJ02} 粗略转为 {@link CoordType#WGS84}
     * @param lat gcj02维度
     * @param lng gcj02经度
     * @return 维经数组（wgs84）
     */
    public static double[] gcj02ToWgs84Fuzzy(double lat, double lng) {
        double[] latLng = new double[2];
        if (outOfChina(lat, lng)) {
            latLng[0] = lat;
            latLng[1] = lng;
            return latLng;
        }
        double[] deltaD = transform(lat, lng);
        latLng[0] = lat - deltaD[0];
        latLng[1] = lng - deltaD[1];
        return latLng;
    }

    /**
     * 用于 {@link CoordType#GCJ02} 精确转为 {@link CoordType#WGS84}
     * @param lat gcj02维度
     * @param lng gcj02经度
     * @return 维经数组（wgs84）
     */
    public static double[] gcj02ToWgs84Precise(double lat, double lng) {
        double initDelta = 0.01;
        double threshold = 0.000000001;
        double dLat = initDelta, dLon = initDelta;
        double mLat = lat - dLat, mLon = lng - dLon;
        double pLat = lat + dLat, pLon = lng + dLon;
        double wgsLat, wgsLon, i = 0;
        while (true) {
            wgsLat = (mLat + pLat) / 2;
            wgsLon = (mLon + pLon) / 2;
            double[] tmp = wgs84ToGcj02(wgsLat, wgsLon);
            dLat = tmp[0] - lat;
            dLon = tmp[1] - lng;
            if ((Math.abs(dLat) < threshold) && (Math.abs(dLon) < threshold)) {
                break;
            }
            if (dLat > 0) {
                pLat = wgsLat;
            } else {
                mLat = wgsLat;
            }
            if (dLon > 0) {
                pLon = wgsLon;
            } else {
                mLon = wgsLon;
            }
            if (++i > 10000) {
                break;
            }
        }
        double[] latLng = new double[2];
        latLng[0] = wgsLat;
        latLng[1] = wgsLon;
        return latLng;
    }

    /**
     * 用于 {@link CoordType#WGS84} 与 {@link CoordType#GCJ02} 坐标转换
     * @param lat 维度
     * @param lng 经度
     * @return 两坐标系间的偏移
     */
    public static double[] transform(double lat, double lng) {
        double[] latLng = new double[2];
        double dLat = transformLat(lng - 105.0, lat - 35.0);
        double dLon = transformLng(lng - 105.0, lat - 35.0);
        double radLat = lat / 180.0 * PI;
        double magic = Math.sin(radLat);
        magic = 1 - OFFSET * magic * magic;
        double sqrtMagic = Math.sqrt(magic);
        dLat = (dLat * 180.0) / ((AXIS * (1 - OFFSET)) / (magic * sqrtMagic) * PI);
        dLon = (dLon * 180.0) / (AXIS / sqrtMagic * Math.cos(radLat) * PI);
        latLng[0] = dLat;
        latLng[1] = dLon;
        return latLng;
    }

    public static double transformLat(double x, double y) {
        double ret = -100.0 + 2.0 * x + 3.0 * y + 0.2 * y * y + 0.1 * x * y + 0.2 * Math.sqrt(Math.abs(x));
        ret += (20.0 * Math.sin(6.0 * x * PI) + 20.0 * Math.sin(2.0 * x * PI)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(y * PI) + 40.0 * Math.sin(y / 3.0 * PI)) * 2.0 / 3.0;
        ret += (160.0 * Math.sin(y / 12.0 * PI) + 320 * Math.sin(y * PI / 30.0)) * 2.0 / 3.0;
        return ret;
    }

    public static double transformLng(double x, double y) {
        double ret = 300.0 + x + 2.0 * y + 0.1 * x * x + 0.1 * x * y + 0.1 * Math.sqrt(Math.abs(x));
        ret += (20.0 * Math.sin(6.0 * x * PI) + 20.0 * Math.sin(2.0 * x * PI)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(x * PI) + 40.0 * Math.sin(x / 3.0 * PI)) * 2.0 / 3.0;
        ret += (150.0 * Math.sin(x / 12.0 * PI) + 300.0 * Math.sin(x / 30.0 * PI)) * 2.0 / 3.0;
        return ret;
    }

    public static boolean outOfChina(double lat, double lon) {
        if (lon < 72.004 || lon > 137.8347) {
            return true;
        }
        return lat < 0.8293 || lat > 55.8271;
    }
}
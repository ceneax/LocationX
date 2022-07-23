package ceneax.app.lib.locationx;

import android.annotation.SuppressLint;
import android.location.GnssStatus;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.location.LocationManagerCompat;

public class LocationCore {
    // 系统单次最长定位时间
    private static final long MAX_SINGLE_LOCATION_TIMEOUT_MS = 30 * 1000;

    private final LocationManager mLocationManager;

    public LocationCore(@NonNull LocationManager locationManager) {
        mLocationManager = locationManager;
    }

    public LocationManager getLocationManager() {
        return mLocationManager;
    }

    public boolean isLocationEnabled() {
        return LocationManagerCompat.isLocationEnabled(mLocationManager);
    }

    public void getLocation(@NonNull ILocationCallback locationCallback) {
        getLocation(0, locationCallback);
    }

    @SuppressLint("MissingPermission")
    public void getLocation(long timeout, @NonNull ILocationCallback locationCallback) {
        try {
            final Location[] bestLocation = {null};
            final boolean[] hasFinished = {false};

            SatelliteStatusListener satelliteStatusListener = new SatelliteStatusListener(mLocationManager);

            if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                LXLog.d("GPS Provider 有效，开始获取 GPS 最后一次定位信息");
                Location location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (LocationUtil.isBetterLocation(location, bestLocation[0])) {
                    bestLocation[0] = location;
                    LXLog.d("本次 GPS Provider 获取的定位信息已记录为高优先级");
                }
            }
            if (mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                LXLog.d("Network Provider 有效，开始获取 Network 最后一次定位信息");
                Location location = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                if (LocationUtil.isBetterLocation(location, bestLocation[0])) {
                    bestLocation[0] = location;
                    LXLog.d("本次 Network Provider 获取的定位信息已记录为高优先级");
                }
            }

            final Location[] finalBestLocation = {bestLocation[0]};
            final LocationListener gpsListener;
            final LocationListener networkListener;

            networkListener = new LocationListener() {
                @Override
                public void onLocationChanged(@NonNull Location location) {
                    LXLog.d("requestSingleUpdate: networkListener 执行回调，已得到定位信息");
                    if (LocationUtil.isBetterLocation(location, finalBestLocation[0])) {
                        finalBestLocation[0] = location;
                        LXLog.d("requestSingleUpdate: networkListener 定位信息已记录为高优先级");
                    }
                    mLocationManager.removeUpdates(this);
                }
            };
            gpsListener = new LocationListener() {
                @Override
                public void onLocationChanged(@NonNull Location location) {
                    LXLog.d("requestSingleUpdate: gpsListener 执行回调，已得到定位信息");
                    if (LocationUtil.isBetterLocation(location, finalBestLocation[0])) {
                        finalBestLocation[0] = location;
                        LXLog.d("requestSingleUpdate: gpsListener 定位信息已记录为高优先级");
                    }
                    mLocationManager.removeUpdates(networkListener);
                    mLocationManager.removeUpdates(this);
                    satelliteStatusListener.removeListener();

                    locationCallback.onResult(finalBestLocation[0]);
                    LXLog.i(finalBestLocation[0].toString());
                    hasFinished[0] = true;
                }
            };

            if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                mLocationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, gpsListener, Looper.getMainLooper());
            }
            if (mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                mLocationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, networkListener, Looper.getMainLooper());
            }

            if (timeout <= 0 || timeout > MAX_SINGLE_LOCATION_TIMEOUT_MS) {
                timeout = MAX_SINGLE_LOCATION_TIMEOUT_MS;
                LXLog.d("未设置超时时间，gpsListener 将使用系统默认超时时间：" + timeout + "ms");
            }

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (!hasFinished[0]) {
                    LXLog.d("超时时间已到，gpsListener 未获取到定位信息");
                    locationCallback.onResult(finalBestLocation[0]);
                    LXLog.i(finalBestLocation[0].toString());
                }

                mLocationManager.removeUpdates(gpsListener);
                mLocationManager.removeUpdates(networkListener);
                satelliteStatusListener.removeListener();
            }, timeout);
        } catch (Exception e) {
            locationCallback.onResult(null);
            LXLog.e("getLocation 获取单次定位失败：" + e.getMessage());
        }
    }

    @SuppressLint("MissingPermission")
    private static class SatelliteStatusListener {
        private final LocationManager mLocationManager;

        private GpsStatus.Listener mGpsListener;
        private GnssStatus.Callback mGnssCallback;

        public SatelliteStatusListener(LocationManager locationManager) {
            mLocationManager = locationManager;

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                mGpsListener = createGpsStatus();
                mLocationManager.addGpsStatusListener(mGpsListener);
            } else {
                mGnssCallback = createGnssStatus();
                mLocationManager.registerGnssStatusCallback(mGnssCallback);
            }
        }

        public void removeListener() {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                if (mGpsListener != null) {
                    mLocationManager.removeGpsStatusListener(mGpsListener);
                }
            } else {
                if (mGnssCallback != null) {
                    mLocationManager.unregisterGnssStatusCallback(mGnssCallback);
                }
            }
        }

        private GpsStatus.Listener createGpsStatus() {
            return event -> {
                if (event == GpsStatus.GPS_EVENT_SATELLITE_STATUS) {
                    // 总卫星个数
                    int satelliteCount = 0;
                    // 达标卫星个数
                    int validSatelliteCount = 0;

                    GpsStatus gpsStatus = mLocationManager.getGpsStatus(null);
                    for (GpsSatellite gpsSatellite : gpsStatus.getSatellites()) {
                        satelliteCount ++;
                        // 信噪比大于25判定为有效定位卫星
                        if (gpsSatellite.getSnr() > 25) {
                            validSatelliteCount ++;
                        }
                    }

                    LXLog.i("总卫星个数: " + satelliteCount + "; 达标卫星个数: " + validSatelliteCount);

                    // 至少三颗有效卫星才判定为满足GPS定位条件
                    if (validSatelliteCount < 3) {
                        LXLog.e("达标卫星个数小于3，不满足定位条件");
                    } else {
                        LXLog.d("达标卫星个数大于等于3，满足定位条件");
                    }
                }
            };
        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        private GnssStatus.Callback createGnssStatus() {
            return new GnssStatus.Callback() {
                @Override
                public void onSatelliteStatusChanged(@NonNull GnssStatus status) {
                    // 总卫星个数
                    int satelliteCount = status.getSatelliteCount();
                    // 达标卫星个数
                    int validSatelliteCount = 0;

                    for (int i = 0; i < satelliteCount; i ++) {
                        // 信噪比大于25判定为有效定位卫星
                        if (status.getCn0DbHz(i) > 25) {
                            validSatelliteCount ++;
                        }
                    }

                    LXLog.i("总卫星个数: " + satelliteCount + "; 达标卫星个数: " + validSatelliteCount);

                    // 至少三颗有效卫星才判定为满足GPS定位条件
                    if (validSatelliteCount < 3) {
                        LXLog.e("达标卫星个数小于3，不满足定位条件");
                    } else {
                        LXLog.d("达标卫星个数大于等于3，满足定位条件");
                    }
                }
            };
        }
    }
}
package ceneax.app.lib.locationx;

import android.app.Application;
import android.content.Context;
import android.location.LocationManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * <ul>
 *     <li>Description: LocationX 主类</li>
 *     <li>Date: 2022-07-26 15:49</li>
 *     <li>Author: ceneax</li>
 * </ul>
 */
public class LocationX {
    private static Application mApp;
    private static CoordType mBaseCoordType = CoordType.WGS84;

    private LocationX() {}

    static void setApplication(@NonNull Application application) {
        mApp = application;
    }

    public static void setLogger(@Nullable ILogPrinter logPrinter) {
        LXLog.setLogPrinter(logPrinter);
    }

    public static void setCoordType(CoordType coordType) {
        mBaseCoordType = coordType;
    }

    public static CoordType getCoordType() {
        return mBaseCoordType;
    }

    public static LocationCore create() {
        return new LocationCore((LocationManager) mApp.getSystemService(Context.LOCATION_SERVICE), mBaseCoordType);
    }
}
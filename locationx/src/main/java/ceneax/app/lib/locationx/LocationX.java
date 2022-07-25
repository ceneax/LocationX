package ceneax.app.lib.locationx;

import android.app.Application;
import android.content.Context;
import android.location.LocationManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class LocationX {
    private static Application mApp;
    private static CoordType mBaseCoordType = CoordType.WGS84;

    private LocationX() {}

    public static void setApplication(@NonNull Application application) {
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
        return new LocationCore((LocationManager) mApp.getSystemService(Context.LOCATION_SERVICE));
    }
}
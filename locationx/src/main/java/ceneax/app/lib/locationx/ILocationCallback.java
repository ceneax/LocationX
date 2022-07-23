package ceneax.app.lib.locationx;

import android.location.Location;

import androidx.annotation.Nullable;

public interface ILocationCallback {
    void onResult(@Nullable Location location);
}
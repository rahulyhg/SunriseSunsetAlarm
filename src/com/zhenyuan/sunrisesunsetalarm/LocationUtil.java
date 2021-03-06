package com.zhenyuan.sunrisesunsetalarm;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

public class LocationUtil {

    private static Location betterLocation = null;
    private static Location lastLocation = null;

    public static double[] getLocalTitudes(final Context context) {

        double[] titudes = null;
        LocationManager manager = null;

        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                // Called when a new location is found by the network
                // location
                // provider.
                if (isBetterLocation(location, lastLocation)) {
                    betterLocation = location;
                }
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };

        try {
            manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            lastLocation = manager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (lastLocation == null)
                lastLocation = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            betterLocation = lastLocation;

            // Register the listener with the Location Manager to receive
            // location updates
            manager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);

            if (betterLocation != null) {
                titudes = new double[] {
                        betterLocation.getLatitude(), betterLocation.getLongitude()
                };
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (manager != null) {
                try{
                    manager.removeUpdates(locationListener);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return titudes;
    }

    private static final int TWO_MINUTES = 1000 * 60 * 2;

    /** Determines whether one Location reading is better than the current Location fix
      * @param location  The new Location that you want to evaluate
      * @param currentBestLocation  The current Location fix, to which you want to compare the new one
      */
    protected static boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
        // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }

    /** Checks whether two providers are the same */
    private static boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
          return provider2 == null;
        }
        return provider1.equals(provider2);
    }
}

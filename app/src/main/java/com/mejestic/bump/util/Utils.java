package com.mejestic.bump.util;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import com.google.android.gms.maps.model.LatLng;

public class Utils {
  public static boolean checkMapPermission(Context context) {
    if (ActivityCompat.checkSelfPermission(context,
        android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
      // TODO: Consider calling
      //    ActivityCompat#requestPermissions
      // here to request the missing permissions, and then overriding
      //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
      //                                          int[] grantResults)
      // to handle the case where the user grants the permission. See the documentation
      // for ActivityCompat#requestPermissions for more details.
      return false;
    }
    return true;
  }

  public static long calculateDistance(Location location1, Location location2) {
    double lat1 = location1.getLatitude();
    double lng1 = location1.getLongitude();
    double lat2 = location2.getLatitude();
    double lng2 = location2.getLongitude();
    double dLat = Math.toRadians(lat2 - lat1);
    double dLon = Math.toRadians(lng2 - lng1);
    double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(Math.toRadians(lat1)) * Math.cos(
        Math.toRadians(lat2)) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
    double c = 2 * Math.asin(Math.sqrt(a));
    long distanceInMeters = Math.round(6371000 * c);
    return distanceInMeters;
  }

  public static long calculateLatLngDistance(LatLng latlng1, LatLng latlng2) {
    double lat1 = latlng1.latitude;
    double lng1 = latlng1.longitude;
    double lat2 = latlng2.latitude;
    double lng2 = latlng2.longitude;
    double dLat = Math.toRadians(lat2 - lat1);
    double dLon = Math.toRadians(lng2 - lng1);
    double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(Math.toRadians(lat1)) * Math.cos(
        Math.toRadians(lat2)) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
    double c = 2 * Math.asin(Math.sqrt(a));
    long distanceInMeters = Math.round(6371000 * c);
    return distanceInMeters;
  }
}

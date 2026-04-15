package edu.njit.njcourts.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;

public class NetworkUtils {

    public enum NetworkType {
        NONE,
        WIFI,
        MOBILE_DATA
    }

    public static NetworkType getNetworkType(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return NetworkType.NONE;

        Network activeNetwork = cm.getActiveNetwork();
        if (activeNetwork == null) return NetworkType.NONE;

        NetworkCapabilities capabilities = cm.getNetworkCapabilities(activeNetwork);
        if (capabilities == null) return NetworkType.NONE;

        if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
            return NetworkType.WIFI;
        } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
            return NetworkType.MOBILE_DATA;
        }

        return NetworkType.NONE;
    }
}

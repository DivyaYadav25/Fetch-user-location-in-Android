package com.example.telyport;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class Utility {
    public static boolean isNetworkAvailable() {
        ConnectivityManager connMgr = (ConnectivityManager) AppApplication.Companion.getMContext().getSystemService(
                Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = null;
        if (connMgr != null) {
            activeNetwork = connMgr.getActiveNetworkInfo();
        }
        return activeNetwork != null && activeNetwork.isAvailable() && activeNetwork.isConnected();
    }
}

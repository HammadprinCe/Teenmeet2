package com.example.teenmeet.activities.models;

import android.webkit.JavascriptInterface;

import com.example.teenmeet.activities.activities.Callactuivity;

public class InterfaceJava {
    Callactuivity callactuivity;
    public  InterfaceJava(Callactuivity callactuivity){
        this.callactuivity=callactuivity;

    }
   @JavascriptInterface
    public void onPeerConnected(){
        callactuivity.onPeerConnected();

    }
}

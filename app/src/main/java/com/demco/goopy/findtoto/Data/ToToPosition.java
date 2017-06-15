package com.demco.goopy.findtoto.Data;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by goopy on 2017-03-23.
 */

public class ToToPosition {

    public static int NONE = 0;
    public static int VISIBLE = 1;
    public static int MODIFY = 2;
    public static int DELETE = 3;

    public static String ERROR_CHANNEL = "error_channel";

    public String uniqueId;
    public String biz;
    public String name;
    public String channel;
    public String phone;
    public int state = NONE;
    public String bizState;
    public List<String> addressList = new ArrayList<>(5);
//    public String[] rawData = new String[LAST_INDEX];
    public String addressData = null;
    public LatLng latLng;
//    public ToToPosition() {
//        for(int i = 0; i < LAST_INDEX; ++i) {
//            rawData[i] = "";
//        }
//    }
}

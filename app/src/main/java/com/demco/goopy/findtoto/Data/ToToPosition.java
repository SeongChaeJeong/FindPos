package com.demco.goopy.findtoto.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by goopy on 2017-03-23.
 */

public class ToToPosition {
    public static int NAME = 0;
    public static int BUSINESS = 1;
    public static int CHANNEL = 2;
    public static int ADDRESS1 = 3;
    public static int ADDRESS2 = 4;
    public static int ADDRESS3 = 5;
    public static int ADDRESS4 = 6;
    public static int ADDRESS5 = 7;
    public static int STATE = 8;
    public static int PHONE = 9;
    public static int LAST_INDEX = 10;

    public int uniqueId = 0;
    public List<String> addressList = new ArrayList<>();
    public String[] rawData = new String[LAST_INDEX];
    public String addressData = null;
    public ToToPosition() {
        for(int i = 0; i < LAST_INDEX; ++i) {
            rawData[i] = "";
        }
    }
}

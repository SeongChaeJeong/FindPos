package com.demco.goopy.findtoto.Data;

import java.util.ArrayList;
import java.util.List;

import io.realm.RealmList;
import io.realm.RealmObject;

import static com.demco.goopy.findtoto.Data.ToToPosition.NONE;

/**
 * Created by goopy on 2017-03-30.
 */

public class ToToPositionRealmObj extends RealmObject {
    public String uniqueId;
    public String targetName;
    public String targetBiz;
    public double latitude;
    public double longtitude;
    public String addressData;
    public String phone;
    public String bizState;
}

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
    private String uniqueId;
    private String targetName;
    private String targetBiz;
    private double latitude;
    private double longtitude;
    private String addressData;
    private String phone;
    private String bizState;

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public String getTargetName() {
        return targetName;
    }

    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }

    public String getTargetBiz() {
        return targetBiz;
    }

    public void setTargetBiz(String targetBiz) {
        this.targetBiz = targetBiz;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongtitude() {
        return longtitude;
    }

    public void setLongtitude(double longtitude) {
        this.longtitude = longtitude;
    }

    public String getAddressData() {
        return addressData;
    }

    public void setAddressData(String addressData) {
        this.addressData = addressData;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getBizState() {
        return bizState;
    }

    public void setBizState(String bizState) {
        this.bizState = bizState;
    }
}

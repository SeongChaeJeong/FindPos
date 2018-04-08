package com.demco.goopy.findtoto.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by goopy on 2017-03-25.
 */

public class PositionDataSingleton {
    private static PositionDataSingleton mInstance = null;

    private boolean GPSRecevie = true;
    private Map<String, Integer> bizCategoryColorIndexs = new HashMap<>();
    private Map<String, Float> bizCategoryColorMap = new HashMap<>();
    private List<ToToPosition> markerPositions = null;
    private List<ToToPosition> markerModifyPositions = null;

    private PositionDataSingleton() {
        markerPositions = new ArrayList<ToToPosition>();
        markerModifyPositions = new ArrayList<ToToPosition>();
    }

    public static PositionDataSingleton getInstance() {
        if(mInstance == null) {
            mInstance = new PositionDataSingleton();
        }
        return mInstance;
    }

    public void setMarkerPositions(List<ToToPosition> list) {
        markerPositions.clear();
        markerPositions.addAll(list);
    }

    public List<ToToPosition> getMarkerPositions() {
        return markerPositions;
    }

    public List<ToToPosition> getMarkerModifyPositions() {
        return markerModifyPositions;
    }

    public Map<String, Integer> getBizCategoryColorIndexs() {
        return bizCategoryColorIndexs;
    }

    public boolean isGPSRecevie() {
        return GPSRecevie;
    }

    public void setGPSRecevie(boolean GPSRecevie) {
        this.GPSRecevie = GPSRecevie;
    }
}

package com.demco.goopy.findtoto.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by goopy on 2017-03-25.
 */

public class PositionDataSingleton {
    private static PositionDataSingleton mInstance = null;

    private List<ToToPosition> markerPositions = null;

    private PositionDataSingleton() {
        markerPositions = new ArrayList<ToToPosition>();
    }

    public static PositionDataSingleton getInstance() {
        if(mInstance == null) {
            mInstance = new PositionDataSingleton();
        }
        return mInstance;
    }

    public List<ToToPosition> getMarkerPositions() {
        return markerPositions;
    }
}

package com.demco.goopy.findtoto.System;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.demco.goopy.findtoto.Utils.FileManager;

/**
 * Created by goopy on 2017-03-28.
 */

public class LoadData_Service extends Service {
    private final IBinder mBinder = new MyBinder();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        FileManager.readExcelFile(this,"address.xls", true);
        Intent i = new Intent("postion_data_update");
        sendBroadcast(i);
        return Service.START_NOT_STICKY;
    }


    public class MyBinder extends Binder {
        public LoadData_Service getService() {
            return LoadData_Service.this;
        }
    }

};

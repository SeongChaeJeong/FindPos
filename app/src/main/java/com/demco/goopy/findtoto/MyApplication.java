package com.demco.goopy.findtoto;

import android.app.Application;
import android.support.multidex.MultiDexApplication;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by goopy on 2017-03-27.
 */
@ReportsCrashes(
        mailTo = "goopy684@gmail.com",
        mode = ReportingInteractionMode.SILENT,
        resToastText = R.string.error_crash
)
public class MyApplication extends MultiDexApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        ACRA.init(this);
        RealmConfiguration config = new RealmConfiguration.Builder(this).build();
        Realm.setDefaultConfiguration(config);
    }
}
